package sg.gov.dh.beacons.estimote;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class EstimoteProximityContent {

    private String title;
    private String subtitle;

    EstimoteProximityContent(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    String getTitle() {
        return title;
    }

    String getSubtitle() {
        return subtitle;
    }
}
