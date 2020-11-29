package com.etl.report.dto;

import java.util.HashMap;

public class ReportSummaryData {

    public HashMap<String, Integer> getTotalMatchCountMap() {
        return totalMatchCountMap;
    }

    public void setTotalMatchCountMap(HashMap<String, Integer> totalMatchCountMap) {
        this.totalMatchCountMap = totalMatchCountMap;
    }

    public HashMap<String, Integer> getTotalDiffCountMap() {
        return totalDiffCountMap;
    }

    public void setTotalDiffCountMap(HashMap<String, Integer> totalDiffCountMap) {
        this.totalDiffCountMap = totalDiffCountMap;
    }

    public HashMap<String, Integer> getTotalSourceNullCountMap() {
        return totalSourceNullCountMap;
    }

    public void setTotalSourceNullCountMap(HashMap<String, Integer> totalSourceNullCountMap) {
        this.totalSourceNullCountMap = totalSourceNullCountMap;
    }

    public HashMap<String, Integer> getTotalTargetNullCountMap() {
        return totalTargetNullCountMap;
    }

    public void setTotalTargetNullCountMap(HashMap<String, Integer> totalTargetNullCountMap) {
        this.totalTargetNullCountMap = totalTargetNullCountMap;
    }

    HashMap<String,Integer >totalMatchCountMap = new HashMap<>();
    HashMap<String,Integer >totalDiffCountMap = new HashMap<>();
    HashMap<String,Integer >totalSourceNullCountMap = new HashMap<>();
    HashMap<String,Integer >totalTargetNullCountMap = new HashMap<>();

    public void passPlusOne(String key)
    {
        if(totalMatchCountMap.containsKey(key))
        {
            int newValue = this.totalMatchCountMap.get(key)+1;
            this.totalMatchCountMap.put(key,newValue);

        }else
        {
            this.totalMatchCountMap.put(key,1);
        }

    }
    public void failPlusOne(String key)
    {
        if(totalDiffCountMap.containsKey(key))
        {
            int newValue = this.totalDiffCountMap.get(key)+1;
            this.totalDiffCountMap.put(key,newValue);

        }else
        {
            this.totalDiffCountMap.put(key,1);
        }
    }




}
