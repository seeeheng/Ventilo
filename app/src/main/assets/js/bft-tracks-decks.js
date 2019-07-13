var map, infoWindow;
var markers=Array();

function initRabbit(mqAddress, username, password, topic, port){
    // var ws = new WebSocket('ws://18.221.97.164:15674/ws');
    var mqAddress = "ws://"+mqAddress+":"+port+"/ws";
    var ws = new WebSocket(mqAddress);
    var client = Stomp.over(ws);
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

    var x = parseFloat(message[0]);
    var y = parseFloat(message[1]);

    var alt = message[2];

    var bearing = message[3];
    var user = message[4];
    var type = message[5];
    var createdTime = message[6];

    if (alt >= lowestHeight && alt <= highestHeight) {
        var marker = getCustomMarker(x, y, type, createdTime, true, 0);

        markers.push(marker);
        marker.addTo(map0);
    }
}

function androidToJSupdateLocation(trackerMessage)
{
    var message=(trackerMessage).split(",");
    updateTarget(message);
}

function updateTarget(message) {

    var x = parseFloat(message[0]);
    var y = parseFloat(message[1]);

    var alt = message[2];

//    alt=parseFloat(alt)-baselineHeightInMetres;
    var bearing = message[3];
    var user = message[4];
    var action = message[5];
    var type = message[6];

    var found = false;

    console.log("JS Received " + message);
    for (i = 0 ; i < markers.length; i++) {

        // console.debug("markers[i]",markers[i]._tooltip._content.valueOf());
        if (markers[i]._tooltip._content.valueOf() == user) {
            console.log("Removing marker: ", markers[i]);
            found = true;
            map0.removeLayer(markers[i]);
            markers.splice(i,1);

            if (alt >= lowestHeight && alt <= highestHeight) {
                var marker = null;
                if (action =='FORWARD') {
                    marker = getCustomMarker(x, y, 'navigating', user, true, bearing);
                } else if (action == 'FIDGETING' | action == 'STATIONARY') {
                    marker = getCustomMarker(x, y, 'standing', user, true, 0);
                } else if (action == 'BEACONDROP') {
                    marker = getCustomMarker(x, y, 'null', user, true, 0);
                }

                console.log("Adding marker: " + user + "," + y + "," + x);
                markers.push(marker);
                marker.addTo(map0);

                // console.debug('Removing marker');
                break;
            }
        }
    }

    if (!found) {
        if (alt >= lowestHeight && alt <= highestHeight) {
            var marker = null;
            console.log("Adding marker: " + user + "," + y + "," + x);

            if (action == 'FORWARD') {
                marker = getCustomMarker(x, y, 'navigating', user, true, bearing);
            } else if (action == 'FIDGETING' | action == 'STATIONARY') {
                marker = getCustomMarker(x, y, 'standing', user, true, 0);
            } else if (action == 'BEACONDROP') {
                marker = getCustomMarker(x, y, 'null', user, true, 0);
            }

            marker.addTo(map0);
            markers.push(marker);
        }
    }
}