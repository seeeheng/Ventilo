var map, infoWindow;
//var markers=Array();

var markerMap = new Map();

//var hashMap = function() {
//    this.hashDict = {};//dictionary
//    this.size = 0;
//    this.debug = true;
//    return this;
//}
//
//hashMap.prototype.put = function(_key, _value){
//    this.hashDict[_key] = _value;
//
//    if (!this.hashDict.hasOwnProperty(_key)) {
////        this.hashDict[_key] = _value;
//        ++this.size;
//    }
//
////    else {
//////        throw 'duplicate keys not allowed. key : '+_key;
////
////    }
//}
//
//hashmap n = new hashMap();

var client;

function initRabbit(mqAddress, username, password, topic, port){
    // var ws = new WebSocket('ws://18.221.97.164:15674/ws');
    var mqAddress = "ws://"+mqAddress+":"+port+"/ws";
    var ws = new WebSocket(mqAddress);
    client = Stomp.over(ws);
    var on_connect = function() {
        console.log('connected');
        // topic="/topic/"+topic;
        id = client.subscribe(topic, function(d) {
            console.log(d.body);
            var message=(d.body).split(",")
            // console.log("Lat:" + message[0]);
            // console.log("Long:" + message[1]);
            // console.log("Alt:" + message[2]);
            // console.log("Bearing:" + message[3]);
            // console.log("WhoAmI:" + message[4]);
            msgName=message[4];
            myname=Android.getName();
            if (myname!=msgName){
                updateTarget(message);
            }
        }, { id: 'decks' });

        client.send(mqTopic, {}, '0,0,0,0,0,BEACONREQUEST,0');
    };
    var on_error =  function(error) {
        console.log(error);
    };
    // client.connect('jax', 'password', on_connect, on_error, 'bfttracks');
    var headers = {
        login:username,
        passcode: password,
        host:'bfttracks'
        // additional header
    };
    client.connect(headers, on_connect, on_error);
}

function disconnectRabbitMQ() {
    client.disconnect(function () {
        console.log('RabbitMQ disconnected');
    });
}

// function initRabbit(mqAddress,username, password){
//
//     var amqp = require('amqplib/callback_api');
//
//     amqp.connect('amqp://jax79sg.hopto.org', function(err, conn) {
//         conn.createChannel(function(err, ch) {
//             var ex = 'bfttracksExchange';
//
//             ch.assertExchange(ex, 'fanout', {durable: false});
//
//             ch.assertQueue('', {exclusive: true}, function(err, q) {
//                 console.log(" [*] Waiting for messages in %s. To exit press CTRL+C", q.queue);
//                 ch.bindQueue(q.queue, ex, '');
//
//                 ch.consume(q.queue, function(msg) {
//                     console.log(" [x] %s", msg.content.toString());
//                     var message=(msg.content.toString()).split(",")
//                     // console.log("Lat:" + message[0]);
//                     // console.log("Long:" + message[1]);
//                     // console.log("Alt:" + message[2]);
//                     // console.log("Bearing:" + message[3]);
//                     // console.log("WhoAmI:" + message[4]);
//                     updateTarget(message);
//                 }, {noAck: true});
//             });
//         });
//     });
// }

function androidToJScreateLocation(trackerMessage)
{
    var message = (trackerMessage).split(",");

    console.log("androidToJScreateLocation, message: " + message);

    var id = message[0];
    var x = parseFloat(message[1]);
    var y = parseFloat(message[2]);
    var alt = message[3];
    var bearing = message[4];
    var user = message[5];
    var type = message[6];
    var createdTime = message[7];

    if (alt >= lowestHeight && alt <= highestHeight) {

        for (var [key, value] of markerMap) {
            if (id == key) {
                console.log("Removing marker: ", value);
                map0.removeLayer(value);
            }

//            console.log(key + " = " + value);
        }

        var marker = getCustomMarker(x, y, type, createdTime, true, 0);

        markerMap.set(id, marker);

        marker.on('click', function(e){
            console.debug("Deleting marker id: " + id, "onClick");
            map0.removeLayer(marker);
            Android.deleteMarker(id);
        });

        marker.addTo(map0);

//        for (i = 0 ; i < markers.length; i++) {
//            if (markers[i]._tooltip._content.valueOf() == user) {
//                var marker = getCustomMarker(x, y, type, createdTime, true, 0);
//
//                markers.push(marker);
//                marker.addTo(map0);
//            }
//        }
    }
}

function androidToJSupdateLocation(trackerMessage)
{
    var message=(trackerMessage).split(",");
    updateTarget(message);
}

function updateTarget(message) {

    var id = message[0];
    var x = parseFloat(message[1]);
    var y = parseFloat(message[2]);
    var alt = message[3];
    var bearing = message[4];
    var user = message[5];
    var action = message[6];
    var type = message[7];
    var deckLevel = message[8];

    console.log("JS Received " + message);

//    if (alt >= lowestHeight && alt <= highestHeight) {
    if (deckLevel == level) {
        for (var [key, value] of markerMap) {
            if (id == key) {
                console.log("Removing marker: ", value);
                map0.removeLayer(value);
                break;
            }

//            console.log(key + " = " + value);
        }

        var marker = null;

        if (action == 'FORWARD') {

            if (type == Android.getOwnString()) {
                marker = getCustomMarker(x, y, 'navigating', user, true, bearing);
            } else {
                marker = getCustomMarker(x, y, 'navigating-stale', user, true, bearing);
            }

        } else if (action == 'FIDGETING' | action == 'STATIONARY') {

            if (type == Android.getOwnString()) {
                marker = getCustomMarker(x, y, 'standing', user, true, 0);
            } else {
                marker = getCustomMarker(x, y, 'standing-stale', user, true, 0);
            }

        } else if (action == 'BEACONDROP') {
            marker = getCustomMarker(x, y, 'null', user, true, 0);
        }

        console.log("Adding marker: " + user + "," + y + "," + x);
        markerMap.set(id, marker);
        marker.addTo(map0);

//        console.debug('Removing marker');
//        break;
    }




//    for (i = 0 ; i < markers.length; i++) {
//
//        // console.debug("markers[i]",markers[i]._tooltip._content.valueOf());
//        if (markers[i]._tooltip._content.valueOf() == user) {
//            console.log("Removing marker: ", markers[i]);
//            found = true;
//            map0.removeLayer(markers[i]);
//            markers.splice(i,1);
//
//            if (alt >= lowestHeight && alt <= highestHeight) {
//                var marker = null;
//                if (action =='FORWARD') {
//                    marker = getCustomMarker(x, y, 'navigating', user, true, bearing);
//                } else if (action == 'FIDGETING' | action == 'STATIONARY') {
//                    marker = getCustomMarker(x, y, 'standing', user, true, 0);
//                } else if (action == 'BEACONDROP') {
//                    marker = getCustomMarker(x, y, 'null', user, true, 0);
//                }
//
//                console.log("Adding marker: " + user + "," + y + "," + x);
//                markers.push(marker);
//                marker.addTo(map0);
//
//                // console.debug('Removing marker');
//                break;
//            }
//        }
//    }

//    if (!found) {
//        if (alt >= lowestHeight && alt <= highestHeight) {
//            var marker = null;
//            console.log("Adding marker: " + user + "," + y + "," + x);
//
//            if (action == 'FORWARD') {
//                marker = getCustomMarker(x, y, 'navigating', user, true, bearing);
//            } else if (action == 'FIDGETING' | action == 'STATIONARY') {
//                marker = getCustomMarker(x, y, 'standing', user, true, 0);
//            } else if (action == 'BEACONDROP') {
//                marker = getCustomMarker(x, y, 'null', user, true, 0);
//            }
//
//            marker.addTo(map0);
//            markers.push(marker);
//        }
//    }
}