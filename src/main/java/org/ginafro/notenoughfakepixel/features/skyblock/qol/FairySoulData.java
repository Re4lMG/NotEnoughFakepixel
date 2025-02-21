package org.ginafro.notenoughfakepixel.features.skyblock.qol;

import java.util.List;
import java.util.Map;

public class FairySoulData {

    String description;
    int soulCount;
    Map<String, List<String>> locations;

    public FairySoulData(String desc, int souls, Map<String,List<String>> locs){
        description = desc;
        soulCount = souls;
        locations = locs;
    }

}
