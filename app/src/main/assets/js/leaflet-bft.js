var baselineHeightInMetres=63.0;

function loadLeaflet(imageoverlay, lowerleftx, lowerlefty, upperrightx, upperrighty, initialx, initialy, initialzoom){
    //leaflet uses (y,x) by default, these 2 functions will convert to xy for easier reading
    var yx = L.latLng;
    var xy = function(x, y) {
        if (L.Util.isArray(x)) {    // When doing xy([x, y]);
            return yx(x[1], x[0]);
        }
        return yx(y, x);  // When doing xy(x, y);
    };

    var map0 = L.map('mapdeck', {
        crs: L.CRS.Simple,
        minZoom: -5,
        maxZoom: 5,
        zoomControl: false,
        zoomSnap: 0.2,
        zoomDelta: 0.2
    });

    //Bounds is always LowerLeft to UpperRight.
    //Vertical zero is set at main deck line.
    //Horizonal zero is set at panel 0.
    //Manually measure the lowerleft and upperright boundaries of the GA cutout in metres based on above vertical zero and hori zero
    var bounds0 = [[xy(lowerleftx,lowerlefty)], [xy(upperrightx,upperrighty)]];
    console.debug(bounds0)
    var image0 = L.imageOverlay(imageoverlay, bounds0).addTo(map0);

    // map0.setView( [5, 80], -3);
    map0.fitBounds(bounds0);
    // map0.flyTo([initialy*factor,initialx*factor],initialzoom);
    map0.setView([initialy*factor,initialx*factor],initialzoom);
    return map0;
}

var hazardIcon = L.icon({
    iconUrl: 'icon_hazard.png',
    iconSize:     [20, 20], // size of the icon
    iconAnchor:   [10, 10], // point of the icon which will correspond to marker's location
    popupAnchor:  [-6, -50], // point from which the popup should open relative to the iconAnchor
    tooltipAnchor:[10, 3]
});

var deceasedIcon = L.icon({
    iconUrl: 'icon_deceased.png',
    iconSize:     [20, 20], // size of the icon
    iconAnchor:   [10, 10], // point of the icon which will correspond to marker's location
    popupAnchor:  [-6, -50], // point from which the popup should open relative to the iconAnchor
    tooltipAnchor:[10, 3]
});

var ownWalkingIcon = L.icon({
    iconUrl: 'icon_own_online_direction.png',

//    iconSize:     [30, 30], // size of the icon
//    iconAnchor:   [15, 29], // point of the icon which will correspond to marker's location
//    popupAnchor:  [-10, -76], // point from which the popup should open relative to the iconAnchor
//    tooltipAnchor:[15, 5]

    iconSize:     [30, 30], // size of the icon
    iconAnchor:   [15, 15], // point of the icon which will correspond to marker's location
//    popupAnchor:  [-10, -76], // point from which the popup should open relative to the iconAnchor
    tooltipAnchor:[15, 5]

//    iconSize:     [20, 20], // size of the icon
//    iconAnchor:   [10, 10], // point of the icon which will correspond to marker's location
////    popupAnchor:  [-10, -76], // point from which the popup should open relative to the iconAnchor
//    tooltipAnchor:[10, 3]
});

var ownStandingIcon = L.icon({
    iconUrl: 'icon_own_online.png',
    iconSize:     [20, 20], // size of the icon
    iconAnchor:   [10, 10], // point of the icon which will correspond to marker's location
    popupAnchor:  [-6, -50], // point from which the popup should open relative to the iconAnchor
    tooltipAnchor:[10, 3]
});

var ownNavigatingIcon = L.icon({
    iconUrl: 'icon_own_online_direction.png',

    iconSize:     [30, 30], // size of the icon
    iconAnchor:   [15, 15], // point of the icon which will correspond to marker's location
//    popupAnchor:  [-10, -76], // point from which the popup should open relative to the iconAnchor
    tooltipAnchor:[15, 5]

//    iconSize:     [20, 20], // size of the icon
//        iconAnchor:   [10, 10], // point of the icon which will correspond to marker's location
//    //    popupAnchor:  [-10, -76], // point from which the popup should open relative to the iconAnchor
//        tooltipAnchor:[10, 3]
});

function getCustomMarker(xMetres, yMetres, markerType, label, isPermanentLabel, angle){
    var icon;
    var solMarker;
    var classStyleName;
    var sol = L.latLng([ (yMetres) * factor, xMetres * factor ]);

    if (markerType == 'standing')
    {
        console.debug("Adding standing icon");
        icon = ownStandingIcon;
        classStyleName = "leafletTooltipLabel-green";

        solMarker= L.marker(sol,{icon: icon, rotationAngle: angle});
    }
    else if (markerType =='navigating')
    {
        console.debug("Adding navigating icon");
        icon = ownNavigatingIcon;
        classStyleName = "leafletTooltipLabel-green";

        solMarker = L.marker(sol,{icon: icon, rotationAngle: angle});
    }
    else if (markerType == Android.getHazardString()) {
        console.debug("Adding hazard icon");
        icon = hazardIcon;
        classStyleName = "leafletTooltipLabel-orange";

        solMarker = L.marker(sol,{icon: icon, rotationAngle: angle});
    }
    else if (markerType == Android.getDeceasedString()) {
        console.debug("Adding deceased icon");
        icon = deceasedIcon;
        classStyleName = "leafletTooltipLabel-grey";

        solMarker = L.marker(sol,{icon: icon, rotationAngle: angle});
    }
    else
    {
        console.debug("Adding default icon");
        solMarker = L.marker(sol,{rotationAngle: angle});
    }

    solMarker.bindTooltip(label, {permanent:isPermanentLabel, className: classStyleName});
    return solMarker;
}

