package dsta.sg.com.ventilo.model;

import lombok.Data;

@Data
public class Blueprint {
    private String name;
    private String resName;
    private int resID;
    private float heightInMetres;
    private float lengthInMetres;
    private int heightInPixels;
    private int lengthInPixels;
}
