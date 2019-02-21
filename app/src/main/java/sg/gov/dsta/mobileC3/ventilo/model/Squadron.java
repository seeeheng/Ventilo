package sg.gov.dsta.mobileC3.ventilo.model;

import java.util.Map;

public class Squadron {

    // Map <team number, call sign of member>
    private Map<Integer, String> team;
//    private int teamNo;
//    private String callSign;

    public Squadron() {
        formTeams();
    }

    private void formTeams() {
        team.put(1, "A11");
        team.put(1, "A22");
        team.put(2, "B11");
        team.put(2, "B22");
        team.put(2, "B33");
        team.put(3, "C11");
        team.put(3, "C22");
    }
}
