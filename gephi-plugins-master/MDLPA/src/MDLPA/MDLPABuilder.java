package MDLPA;

import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererBuilder;
import org.gephi.clustering.spi.ClustererUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * Builds up an instance of MDLPA with the selected configuration.
 * 
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
@ServiceProvider(service = ClustererBuilder.class)
public class MDLPABuilder implements ClustererBuilder {

    MDLPAUI settingsUI = new MDLPAUI();
    
    @Override
    public Clusterer getClusterer() {
      return new MDLPA();
    }

    @Override
    public String getName() {
        return "MDLPA";
    }

    @Override
    public String getDescription() {
        return "A plugin that provides an implementation of the MDLPA algorithm for community detection in multidimensional networks described in :\n" +
            "Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks.\n" +
            "ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51.";
    }

    @Override
    public Class getClustererClass() {
      return MDLPA.class;
    }

    @Override
    public ClustererUI getUI() {
      return settingsUI;
    }
}