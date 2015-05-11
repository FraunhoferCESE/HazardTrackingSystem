package org.fraunhofer.plugins.hts.document;

import java.util.Comparator;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
 
public class ControlNumberComparator implements Comparator<Hazard_Controls> {
 
    @Override
    public int compare(Hazard_Controls x, Hazard_Controls y) {
      return Integer.compare(x.getControlNumber(), y.getControlNumber());
    }
}