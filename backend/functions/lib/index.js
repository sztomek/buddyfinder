"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const functions = require("firebase-functions");
// import * as algoliasearch from 'algoliasearch'
const admin = require("firebase-admin");
const express = require("express");
const parser = require("body-parser");
admin.initializeApp(functions.config().firebase);
const firestore = admin.firestore();
const app = express();
app.use(parser.json());
// `Authorization: Bearer <Firebase ID Token>`.
const authenticate = (req, res, next) => {
    if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer ')) {
        res.status(403).send('Unauthorized');
        return;
    }
    const idToken = req.headers.authorization.split('Bearer ')[1];
    admin.auth().verifyIdToken(idToken).then(decodedIdToken => {
        req.userid = decodedIdToken.uid;
        next();
    }).catch(error => {
        res.status(403).send('Unauthorized');
    });
};
app.use(authenticate);
// const ALGOLIA_INDEX = "users";
// const searchClient = algoliasearch(functions.config().algolia.app_id, functions.config().algolia.api_key);
// const index = searchClient.initIndex(ALGOLIA_INDEX);
//-- AUTH --
exports.storeUser = functions.auth.user().onCreate(event => {
    const data = event.data;
    return firestore.collection('users').doc(data.uid).set({
        displayName: data.displayName || "",
        email: data.email || "",
        photoUrl: data.photoURL || "https://image.freepik.com/free-icon/profile-user-silhouette_318-40557.jpg"
    })
        .then(() => {
        console.log(`User successfully added: ${data.uid}`);
    })
        .catch(error => {
        console.error(`Faield to add user[${data.uid}]: ${error}`);
    });
});
exports.deleteUser = functions.auth.user().onDelete(event => {
    const data = event.data;
    return firestore.collection('users').doc(data.uid).delete()
        .then(() => {
        return Promise.all([
            deleteCollection(firestore.collection(`users/${data.uid}/gcmTokens`), 10),
            deleteQuery(firestore.collection(`locations`)
                .where('from', '==', `${data.uid}`), 10),
            deleteCollection(firestore.collection(`users/${data.uid}/connections`), 10),
            Promise.all([
                firestore.collection('connectionHelpers')
                    .where('user1', '==', `${data.uid}`)
                    .get(),
                firestore.collection('connectionHelpers')
                    .where('user2', '==', `${data.uid}`)
                    .get()
            ]).then(results => {
                const updates = [];
                results[0].docs.forEach((snap, idx) => {
                    const userId = snap.data().user2;
                    updates.push(firestore.doc(`users/${userId}/connections/${data.uid}`)
                        .delete());
                });
                results[1].docs.forEach((snap, idx) => {
                    const userId = snap.data().user1;
                    updates.push(firestore.doc(`users/${userId}/connections/${data.uid}`)
                        .delete());
                });
                updates.push(deleteQuery(firestore.collection('connectionHelpers')
                    .where('user1', '==', `${data.uid}`), 10));
                updates.push(deleteQuery(firestore.collection('connectionHelpers')
                    .where('user2', '==', `${data.uid}`), 10));
                return updates;
            }),
            deleteQuery(firestore.collection(`connectionRequests`)
                .where('from', '==', `${data.uid}`), 10),
            deleteQuery(firestore.collection(`connectionRequests`)
                .where('to', '==', `${data.uid}`), 10)
        ])
            .then(results => {
            console.log(`User[${data.uid}] successfully deleted`);
        });
    })
        .catch(error => {
        console.error(`Faield to delete user[${data.uid}]: ${error}`);
    });
});
function deleteQuery(query, batchSize) {
    return new Promise((resolve, reject) => {
        deleteQueryBatch(query.limit(batchSize), batchSize, resolve, reject);
    });
}
function deleteCollection(collectionRef, batchSize) {
    const query = collectionRef.orderBy('__name__').limit(batchSize);
    return new Promise((resolve, reject) => {
        deleteQueryBatch(query, batchSize, resolve, reject);
    });
}
function deleteQueryBatch(query, batchSize, resolve, reject) {
    query.get()
        .then((snapshot) => {
        // When there are no documents left, we are done
        if (snapshot.empty) {
            return 0;
        }
        // Delete documents in a batch
        const batch = firestore.batch();
        snapshot.docs.forEach((doc) => {
            batch.delete(doc.ref);
        });
        return batch.commit().then(() => {
            return snapshot.size;
        });
    }).then((numDeleted) => {
        if (numDeleted === 0) {
            resolve();
            return;
        }
        // Recurse on the next process tick, to avoid
        // exploding the stack.
        process.nextTick(() => {
            deleteQueryBatch(query, batchSize, resolve, reject);
        });
    })
        .catch(reject);
}
//-- END AUTH --
//-- FireStore --
/**
 * Sends push to the target user when a new connection request is inserted to the database.
 */
exports.sendNewConnectionNotification = functions.firestore.document('connectionRequests/{requestId}').onCreate(event => {
    const record = event.data.data();
    const fromId = record.from;
    const toId = record.to;
    const toDeviceTokensPromise = firestore.collection(`users/${toId}/gcmTokens`).get();
    const fromProfilePromise = firestore.doc(`users/${fromId}`).get();
    return Promise.all([toDeviceTokensPromise, fromProfilePromise])
        .then(results => {
        const tokenRecords = results[0];
        const profile = results[1].data();
        if (!tokenRecords || tokenRecords.empty) {
            return Promise.reject(new Error(`There are no notification tokens for user[${toId}]`));
        }
        if (!profile) {
            return Promise.reject(new Error(`From user[${fromId}] profile not found!`));
        }
        console.log(`From user profile fetched: [${JSON.stringify(profile)}]`);
        const payload = {
            data: {
                displayName: `${profile.displayName}`,
                photoUrl: `${profile.photoUrl}`,
                type: 'CONNECTION_REQUEST',
                connectionRequestId: `${event.params.requestId}`
            }
        };
        return sendMessageAndCleanupTokens(tokenRecords.docs, payload);
    });
});
/**
 * Sends push to the target user when a new connection has been established.
 */
exports.sendConnectionEstablishedNotification = functions.firestore.document('connectionHelpers/{connectionId}').onCreate(event => {
    const record = event.data.data();
    const userId = record.user1; // user1 is connectionRequest.from
    const otherUserId = record.user2; // user2 is connectionRequest.to
    const toDeviceTokensPromise = firestore.collection(`users/${userId}/gcmTokens`).get();
    const fromProfilePromise = firestore.doc(`users/${otherUserId}`).get();
    return Promise.all([toDeviceTokensPromise, fromProfilePromise])
        .then(results => {
        const tokenRecords = results[0];
        const profile = results[1].data();
        if (!tokenRecords || tokenRecords.empty) {
            return Promise.reject(new Error(`There are no notification tokens for user[${otherUserId}]`));
        }
        if (!profile) {
            return Promise.reject(new Error(`From user[${userId}] profile not found!`));
        }
        console.log(`From user profile fetched: [${JSON.stringify(profile)}]`);
        const payload = {
            data: {
                displayName: `${profile.displayName}`,
                photoUrl: `${profile.photoUrl}`,
                type: "NEW_CONNECTION",
                profileId: `${results[1].id}`
            }
        };
        return sendMessageAndCleanupTokens(tokenRecords.docs, payload);
    })
        .catch(error => {
        console.error(`${error}`);
    });
});
/**
 * Sends location response notification when a requested location is inserted into db.
 */
exports.sendLocationResponseNotification = functions.firestore.document('locations/{locationId}').onCreate(event => {
    const snap = event.data.data();
    const to = snap.to;
    const from = snap.from;
    if (!to) {
        return Promise.resolve();
    }
    const toDeviceTokensPromise = firestore.collection(`users/${to}/gcmTokens`).get();
    const fromProfilePromise = firestore.doc(`users/${from}`).get();
    return Promise.all([toDeviceTokensPromise, fromProfilePromise])
        .then(results => {
        const tokenRecords = results[0];
        const profile = results[1].data();
        if (!tokenRecords || tokenRecords.empty) {
            return Promise.reject(new Error(`There are no notification tokens for user[${to}]`));
        }
        if (!profile) {
            return Promise.reject(new Error(`From user[${from}] profile not found!`));
        }
        console.log(`From user profile fetched: [${JSON.stringify(profile)}]`);
        const payload = {
            data: {
                displayName: `${profile.displayName}`,
                photoUrl: `${profile.photoUrl}`,
                type: 'LOCATION_RESPONSE',
                locationId: `${event.data.id}`
            }
        };
        return sendMessageAndCleanupTokens(tokenRecords.docs, payload);
    })
        .catch(error => {
        console.error(`${error}`);
    });
});
function sendMessageAndCleanupTokens(tokenCollectionSnap, payload) {
    const tokens = [];
    tokenCollectionSnap.forEach((value, index) => { tokens[index] = value.id; });
    console.log(`Sending notification [${JSON.stringify(payload)}] to: [${tokens.toString()}]`);
    return admin.messaging().sendToDevice(tokens, payload)
        .then(response => {
        const tokenRecordsToRemove = [];
        response.results.forEach((res, idx) => {
            const error = res.error;
            if (error) {
                console.error(`Failed to send push to token [${tokens[idx]}]: error [${error}]`);
                if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                    tokenRecordsToRemove.push(tokenCollectionSnap[idx].ref.delete());
                }
            }
        });
        return Promise.all(tokenRecordsToRemove);
    })
        .catch(error => {
        console.error(`Error while sending push messages: ${error}`);
        return Promise.reject(error);
    });
}
;
//-- END FIRESTORE --
//-- HTTPS --
/**
 * Handle for createConnectionRequest POSTs.
 */
// https://<base_url>/createConnectionRequest
// body
// {
//		"userId": <userid>
// }
app.post('/createConnectionRequest', (req, resp) => {
    const userId = req.body.userId;
    const fromId = req.userid;
    if (!userId) {
        console.error('Request\'s body doesn\'t have from field');
        return resp.status(400).json({ error: 'FROM_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    const fromUser = firestore.doc(`users/${fromId}`).get();
    const toUser = firestore.doc(`users/${userId}`).get();
    const hasPendingConnection = firestore.collection('connectionRequests')
        .where('from', '==', `${fromId}`)
        .where('to', '==', `${userId}`).get();
    const hasPendingConnection2 = firestore.collection('connectionRequests')
        .where('from', '==', `${userId}`)
        .where('to', '==', `${fromId}`).get();
    const connectedAlready = firestore.collection('connectionHelpers')
        .where('user1', '==', `${fromId}`)
        .where('user2', '==', `${userId}`).get();
    const connectedAlready2 = firestore.collection('connectionHelpers')
        .where('user1', '==', `${userId}`)
        .where('user2', '==', `${fromId}`).get();
    let connectionRequest = null;
    return Promise.all([
        fromUser,
        toUser,
        hasPendingConnection,
        hasPendingConnection2,
        connectedAlready,
        connectedAlready2
    ])
        .then(results => {
        const hasPending = !results[2].empty;
        const hasPending2 = !results[3].empty;
        const connected = !results[4].empty;
        const connected2 = !results[5].empty;
        // check if user exists
        if (!results[1].exists) {
            return Promise.reject(`User [${userId}] doesn't exist!`);
        }
        // check if there's already a connection request in db
        let connectionRequestId;
        if (hasPending) {
            connectionRequestId = results[2].docs[0].id;
        }
        if (hasPending2) {
            connectionRequestId = results[3].docs[0].id;
        }
        if (connectionRequestId) {
            return Promise.reject(`There\'s already a pending connection between [${fromId}] and [${userId}]: connectionRequests/[${connectionRequestId}]`);
        }
        // check if there's already a connection in db
        let connectionId;
        if (connected) {
            connectionId = results[4].docs[0].id;
        }
        if (connected2) {
            connectionId = results[5].docs[0].id;
        }
        if (connectionId) {
            return Promise.reject(`There\'s already a connection between [${fromId}] and [${userId}]: connectionHelpers/[${connectionId}]`);
        }
        // ok, store new connectionRequest
        connectionRequest = {
            from: `${fromId}`,
            fromDisplayName: `${results[0].data().displayName}`,
            to: `${userId}`,
            toDisplayName: `${results[1].data().displayName}`,
            created: new Date()
        };
        return firestore.collection(`connectionRequests`).add(connectionRequest);
    })
        .then((addResult) => {
        console.log(`Successfully added new connectionRequest [${addResult.path}]`);
        connectionRequest.id = addResult.id;
        connectionRequest.created = connectionRequest.created.getTime();
        return resp.status(200).json({ result: connectionRequest });
    })
        .catch(error => {
        console.error(`createConnectionRequest ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle for cancelConnection POSTs.
 */
// https://<base_url>/cancelConnectionRequest?requestId=<value>
//
app.post('/cancelConnectionRequest', (req, resp) => {
    const requestId = req.query.requestId;
    const from = req.userid;
    if (!requestId) {
        console.error('Request doesn\'t have requestId query param');
        return resp.status(400).json({ error: 'REQUEST_ID_QUERY_MISSING' });
    }
    return firestore.doc(`connectionRequests/${requestId}`).get()
        .then(connReq => {
        if (!connReq.exists) {
            return Promise.reject(`ConnectionRequest [${connReq.id}] doesn't exist`);
        }
        if (connReq.data().from === from) {
            return connReq.ref.delete();
        }
        else {
            return Promise.reject(`ConnectionRequest [${connReq.id}] doesn\'t have user [${from}] as from`);
        }
    })
        .then((result) => {
        console.log(`ConnectionRequest successfully deleted.`);
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.error(`cancelConnectionRequest ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle for acceptConnectionRequest POSTs.
 */
// https://<base_url>/acceptConnectionRequest?requestId=<value>
app.post('/acceptConnectionRequest', (req, resp) => {
    const requestId = req.query.requestId;
    const userId = req.userid;
    if (!requestId) {
        console.error('Request doesn\'t have requestId query param');
        return resp.status(400).json({ error: 'REQUEST_ID_QUERY_MISSING' });
    }
    const deleteConnectionReqRef = firestore.doc(`connectionRequests/${requestId}`);
    let connectionHelper = null;
    // need to create the connection on path /connections/{connId}
    return firestore.doc(`connectionRequests/${requestId}`).get()
        .then(connReq => {
        if (!connReq.exists) {
            return Promise.reject(new Error(`ConnectionRequest [${requestId}] doesn\'t exist`));
        }
        const connSnap = connReq.data();
        const fromId = connSnap.from;
        const toId = connSnap.to;
        if (!(toId === userId)) {
            return Promise.reject(`Only the TO user of the original connectionRequest can handle the request: original.to [${toId}], from [${userId}]`);
        }
        return Promise.all([
            firestore.doc(`users/${fromId}`)
                .get(),
            firestore.doc(`users/${toId}`)
                .get()
        ]).then(users => {
            if (!users[0].exists) {
                return Promise.reject(`User [${fromId}] doesn't exist!`);
            }
            if (!users[1].exists) {
                return Promise.reject(`User [${toId}] doesn't exist!`);
            }
            connectionHelper = {
                user1: `${fromId}`,
                user2: `${toId}`
            };
            return Promise.all([
                firestore.collection('connectionHelpers').add(connectionHelper),
                firestore.doc(`users/${fromId}/connections/${toId}`).set({
                    created: new Date(),
                    level: 0,
                    displayName: `${users[0].data().displayName}`
                }),
                firestore.doc(`users/${toId}/connections/${fromId}`).set({
                    created: new Date(),
                    level: 0,
                    displayName: `${users[1].data().displayName}`
                })
            ]);
        });
    })
        .then((addResults) => {
        console.log(`[${connectionHelper.user2}] has accepted the connectionRequest from [${connectionHelper.user1}], new connection record created [${addResults[0].path}]`);
        connectionHelper.id = addResults[0].id;
        return deleteConnectionReqRef.delete();
    })
        .then(allRes => {
        return resp.status(200).json({ result: connectionHelper });
    })
        .catch(error => {
        console.error(`handleConnectionRequest ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle for declineConnectionRequest POSTs.
 */
// https://<base_url>/declineConnectionRequest?requestId=<value>
app.post('/declineConnectionRequest', (req, resp) => {
    const requestId = req.query.requestId;
    const userId = req.userid;
    if (!requestId) {
        console.error('Request doesn\'t have requestId query param');
        return resp.status(400).json({ error: 'REQUEST_ID_QUERY_MISSING' });
    }
    const deleteConnectionReqRef = firestore.doc(`connectionRequests/${requestId}`);
    // need to create the connection on path /connections/{connId}
    return firestore.doc(`connectionRequests/${requestId}`).get()
        .then(connReq => {
        if (!connReq.exists) {
            return Promise.reject(new Error(`ConnectionRequest [${requestId}] doesn\'t exist`));
        }
        const connSnap = connReq.data();
        const toId = connSnap.to;
        if (!(toId === userId)) {
            return Promise.reject(`Only the TO user of the original connectionRequest can handle the request: original.to [${toId}], from [${userId}]`);
        }
        return deleteConnectionReqRef.delete();
    })
        .then(deleteRes => {
        console.log(`connectionRequest [${requestId}] has been declined`);
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.error(`handleConnectionRequest ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle for updateProfile POSTs.
 */
// https://<base_url>/updateProfile?profileId=<profId>
// body {
//	"displayName": "",
//  "email": ""
//  "photoUrl": ""
// }
app.post('/updateProfile', (req, resp) => {
    const userId = req.query.profileId;
    const displayName = req.body.displayName;
    const email = req.body.email;
    const photoURL = req.body.photoUrl;
    const fromId = req.userid;
    if (!userId) {
        console.error('Request doesn\'t have profileId query param');
        return resp.status(400).json({ error: 'PROFILE_ID_QUERY_MISSING' });
    }
    if (!displayName) {
        console.error('Request\'s body doesn\'t have displayName field');
        return resp.status(400).json({ error: 'DISPLAYNAME_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    if (!photoURL) {
        console.error('Request\'s body doesn\'t have photoUrl field');
        return resp.status(400).json({ error: 'PHOTO_URL_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    const userRef = firestore.doc(`users/${userId}`)
        .get();
    const connectionReqs1 = firestore.collection(`connectionRequests`)
        .where('from', '==', `${userId}`)
        .get();
    const connectionReqs2 = firestore.collection(`connectionRequests`)
        .where('to', '==', `${userId}`)
        .get();
    const connections1 = firestore.collection(`connectionHelpers`)
        .where('user1', '==', `${userId}`)
        .get();
    const connections2 = firestore.collection(`connectionHelpers`)
        .where('user2', '==', `${userId}`)
        .get();
    return Promise.all([
        userRef,
        connectionReqs1,
        connectionReqs2,
        connections1,
        connections2
    ])
        .then(results => {
        const userSnap = results[0];
        if (!(userSnap.id === fromId)) {
            return Promise.reject(`User [${fromId}] can't update other user's profile [${userId}]`);
        }
        const profileModel = {
            displayName: `${displayName}`,
            photoUrl: `${photoURL}`
        };
        if (email) {
            profileModel[email] = `${email}`;
        }
        const updates = [];
        updates.push(results[0].ref.update(profileModel));
        results[1].docs.forEach((snapShot, index) => {
            updates.push(snapShot.ref.update({
                fromDisplayName: `${displayName}`
            }));
        });
        results[2].docs.forEach((snapShot, index) => {
            updates.push(snapShot.ref.update({
                toDisplayName: `${displayName}`
            }));
        });
        results[3].docs.forEach((snapShot, index) => {
            updates.push(snapShot.ref.update({
                displayName: `${displayName}`
            }));
        });
        results[4].docs.forEach((snapShot, index) => {
            updates.push(snapShot.ref.update({
                displayName: `${displayName}`
            }));
        });
        return Promise.all(updates);
    })
        .then(() => {
        console.log(`user record [${userId}] successfully updated`);
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.error(`updateProfile ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle for requestLocaiton POSTs. First we need to check the connection status of the participants.
 * A: if the sender peer is trusted by the receiver, simply send a data notification to the receiver. The app will send the position automatically back to server.
 * B: if the sender peer is not trusted, then send a notifiaction to the receiver peer where the receiver can decide to provide the location or not. The result is also sent back to server.
 */
// https://<base_url>/requestLocation
// body
// {
//    "userId": <userId>
// }
app.post('/requestLocation', (req, resp) => {
    const fromId = req.userid;
    const toId = req.body.userId;
    if (!toId) {
        console.error('Request\'s body doesn\'t have to field');
        return resp.status(400).json({ error: 'TO_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    return firestore.doc(`users/${toId}/connections/${fromId}`)
        .get()
        .then(connSnap => {
        if (!connSnap.exists) {
            return Promise.reject(new Error(`Connection doesn\'t exist between [${fromId}] and [${toId}]`));
        }
        return sendLocationNotification(connSnap.data().level, fromId, toId);
    }).then(sendResult => {
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.log(`requestLocation ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
function sendLocationNotification(level, fromId, toId) {
    const getFromProfilePromise = firestore.doc(`users/${fromId}`).get();
    const getToGcmTokensPromise = firestore.collection(`users/${toId}/gcmTokens`).get();
    return Promise.all([getFromProfilePromise, getToGcmTokensPromise])
        .then(results => {
        const profileSnap = results[0].data();
        const tokensSnap = results[1];
        if (!tokensSnap || tokensSnap.empty) {
            return Promise.reject(new Error(`User [${toId}] doesn\'t have any gcm tokens`));
        }
        if (!profileSnap) {
            return Promise.reject(new Error(`User [${fromId}] doesn\'t exist`));
        }
        let payload;
        if (level === 0) {
            // regular, need to send user notif
            payload = {
                data: {
                    displayName: `${profileSnap.displayName}`,
                    photoUrl: `${profileSnap.photoUrl}`,
                    type: 'LOCATION_REQUEST_NOT_TRUSTED',
                    fromId: `${fromId}`
                }
            };
        }
        else if (level === 1) {
            // trusted, need to send data notif
            payload = {
                data: {
                    type: 'LOCATION_REQUEST_TRUSTED',
                    fromId: `${fromId}`
                }
            };
        }
        if (payload) {
            return sendMessageAndCleanupTokens(tokensSnap.docs, payload);
        }
        return Promise.reject(new Error(`Level [${level}] passed to sendLocationNotification is invalid!`));
    });
}
;
/**
 * Handle to add gcm token to a user profile.
 */
// https://<base_url>/addToken
// body: {
//		"token": <string>
// }
app.post('/addToken', (req, resp) => {
    const userId = req.userid;
    const token = req.body.token;
    if (!token) {
        console.error('Request\'s body doesn\'t have token field');
        return resp.status(400).json({ error: 'TOKEN_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    return firestore.doc(`users/${userId}/gcmTokens/${token}`).set({
        created: new Date()
    }).then(() => {
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.error(`addToken ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle for location uploads.
 */
// https://<base_url>/uploadLocation
// body: {
//		"to": <userid>,  -- optional, if it is set, then this is a response to a private location request
//      "public": <boolean>,
//      "lat": <double>,
//      "lng": <double>,
//      "timestamp": <long>
// }
app.post('/uploadLocation', (req, resp) => {
    const from = req.userid;
    const to = req.body.to;
    const isPublic = req.body.public;
    const lat = req.body.lat;
    const lng = req.body.lng;
    const timestamp = req.body.timestamp;
    if (!lat || !lng) {
        console.error('Request\'s body doesn\'t have lat or lng field');
        return resp.status(400).json({ error: 'LAT_LNG_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    if (!to && !isPublic) {
        console.error('Request\'s body must have to or public field');
        return resp.status(400).json({ error: 'TO_OR_PUBLIC_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    return firestore.collection('locations').add({
        from: `${from}`,
        to: `${to || ""}`,
        public: (isPublic || false),
        timestamp: timestamp || new Date().getTime(),
        latLng: new admin.firestore.GeoPoint(lat, lng)
    })
        .then((result) => {
        console.log(`Location successfully added [${result.id}]`);
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.error(`updateLocation ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle to remove connections.
 */
// https://<base_url>/disconnect
// body: {
//		"userId": <userid>
// }
app.post('/disconnect', (req, resp) => {
    const initiator = req.userid;
    const connected = req.body.userId;
    if (!connected) {
        console.error('Request\'s body doesn\'t have connected field');
        return resp.status(400).json({ error: 'CONNECTED_BODY_MISSING', body: `${JSON.stringify(req.body)}` });
    }
    const connection1 = firestore.collection('connectionHelpers')
        .where('user1', '==', `${initiator}`)
        .where('user2', '==', `${connected}`);
    const connection2 = firestore.collection('connectionHelpers')
        .where('user1', '==', `${connected}`)
        .where('user2', '==', `${initiator}`);
    const locations = firestore.collection('locations')
        .where('from', '==', `${initiator}`)
        .where('to', '==', `${connected}`);
    return Promise.all([
        deleteQuery(connection1, 10),
        deleteQuery(connection2, 10),
        deleteQuery(locations, 10),
        firestore.doc(`users/${initiator}/connections/${connected}`).delete(),
        firestore.doc(`users/${connected}/connections/${initiator}`).delete()
    ])
        .then((results) => {
        console.log(`Successfully deleted connection between [${initiator}] and [${connected}] and all the private locations.`);
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.error(`disconnect ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
/**
 * Handle for updateConnection POSTs.
 */
// https://<base_url>/updateConnection
// body {
// 		"level": <int>  -- 0 = normal, 1 = trusted
//      "userId": <String>
// }
app.post('/updateConnection', (req, resp) => {
    const sender = req.userid;
    const userId = req.body.userId;
    const level = req.body.level;
    if (!userId) {
        console.error(`Request\'s body doesn\'t have userId field`);
        return resp.status(400).json({ error: 'USERID_BODY_MISSING' });
    }
    if (level === null) {
        console.error(`Request\'s body doesn\'t have level field`);
        return resp.status(400).json({ error: 'LEVEL_BODY_MISSING' });
    }
    return firestore.doc(`users/${sender}/connections/${userId}`).get()
        .then(connection => {
        if (!connection.exists) {
            return Promise.reject(`Connection [${connection.ref.path}] doesn\'t exist`);
        }
        return firestore.doc(`users/${sender}/connections/${userId}`).update({
            level: level
        });
    })
        .then((result) => {
        console.log(`connection successfully updated`);
        return resp.status(200).json({ result: 'OK' });
    })
        .catch(error => {
        console.error(`updateConnection ran into an error [${error}]`);
        return resp.status(500).json({ error: `${error}` });
    });
});
exports.api = functions.https.onRequest(app);
//-- FIRESTORE --
// export const createIndex = functions.firestore.document("users/{userId}").onCreate(event => {
// 	return addOrUpdateIndex(event.params.userId, event.data.data());
// });
// export const updateIndex = functions.firestore.document("users/{userId}").onUpdate(event => {
// 	return addOrUpdateIndex(event.params.userId, event.data.data());
// });
// export const deleteIndex = functions.firestore.document("users/{userId}").onDelete(event => {
// 	return deleteIndex(event.params.userId);
// });
//-- ALGOLIA --
// doesn't work until a billing account is added to firebase :(
// function addOrUpdateIndex(id, record) {
// 	const userRecord = record;
// 	userRecord.objectID = id;
// 	return index.saveObject(userRecord)
// 		.then(() => {
// 			console.log("User record["+id+"] saved in Algolia");
// 		})
// 		.catch(error => {
// 			console.error("Failed to save user record ["+id+"] in Algolia: " + error) ;
// 		})
// }
// function deleteIndex(id) {
// 	return index.deleteObject(id)
// 		.then(() => {
// 			console.log("User record["+id+"] deleted from Algoria");
// 		})
// 		.catch(error => {
// 			console.error("Failed to delete user record["+id+"] from Algolia: " + error);
// 		});
// } 
//# sourceMappingURL=index.js.map