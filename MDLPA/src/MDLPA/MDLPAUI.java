package MDLPA;

import org.gephi.clustering.spi.ClustererUI;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.gephi.clustering.spi.Clusterer;

/**
 * Used by the gephi platform to initialize the settings panel screen for MDLPA.
 * @author Oualid Boutemine <boutemine.oualid@courrier.uqam.ca>
 * 
 * [1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51. 
 */
public class MDLPAUI implements ClustererUI {
    MDLPASettingsPanel panel = null;
    MDLPA clusterer = null;

    public MDLPAUI() {
      initComponents();
    }

    @Override
    public JPanel getPanel() {
      return panel;
    }

    @Override
    public void setup(Clusterer cluster) {
        this.clusterer = (MDLPA)cluster;
    }
    
    @Override
    public void unsetup() {
        if (this.clusterer == null){
            return;
        }

        this.clusterer.setPrintNodeClusterMemberships(this.panel.chkDisplayNodeMemberships.isSelected());
        this.clusterer.setPrintClustersAndRelevantDimensions(this.panel.chkDisplayClustersAndRelevantDimensions.isSelected());
        this.clusterer.setDimensionsSeparator(this.panel.txtDimensionsSeparator.getText());
    }

    private void initComponents() {
        panel = new MDLPASettingsPanel();
      
        panel.setLayout(
            new BoxLayout(
                panel,
                BoxLayout.Y_AXIS
            )
      );
    }
}