Multidimensional Label Propagation Algorithm for Gephi
======================================================
This repository documents and supports the gephi 0.8.2 implementation of the MDLPA community detection algorithm in multidimensional networks [1].

Unlike the latest version of Gephi (0.9.1 when this plugin was written), which, infortunately doesn't offer a clustering module, Gephi 0.8.2 provides a clustering module/api at the expense of a lacking multigraphs support.
The trick to overcome this limitation was to represent the multidimensional information as aggregate metadata on the edges of adjacent node pairs.
For instance, if the pair of nodes (n1, n2) is linked by 3 edges belonging to dimensions d1, d3 and d5 respectively, the multigraph links will be collapsed
into a single edge (n1, n2) with an edge label/metadata 'd1,d2,d5' pointing to the connecting dimensions which can easily be processed by the gephi platform.

The code for the plugin is available under the MDLPA folder.
The gephi platform code is available under the gephi-plugins folder which was forked from the official gephi plugins repository (https://github.com/gephi/gephi-plugins branch 0.8.2).

Using the plugin
=================
Option 1. If you intend to check the code of the algorithm and eventually play with it: 
- git clone this repository on your machine.
- Make sure you have the JDK and the netbeans IDE installed.
- From netbeans, open the projects gephi plugins and MDLPA
- clean, build and run.

For more details on how to use gephi and the netbeans platform, check out this page: https://github.com/gephi/gephi/wiki/How-to-build-Gephi

Option 2. If you want to use it without modifications.
- Download the gephi software version 0.8.2 (Checkout https://github.com/gephi/gephi/releases for details).
- Install the MDLPA nbm plugin binary available in MDLPA/build/MDLPA.nbm
  - Open gephi, go to tools->plugins.
  - Select the downloaded tab.
  - Click Add plugins and locate the MDLBA.nbm binary.
  - Follow the installation wizard's instructions.
  - Restart the software if asked to do so.

- You should now be able to see MDLPA in the clustering module window (if not shown, go to windows->clustering)
- Select MDLPA from the clustering algorithms list.
- Optionally, you can open the settings window to configure pre/post processing options.
- Import your MDLPA-compatible node/edge csv data from the Data laboratory window.
- Run the algorithm.

Change Log
============
v1.0 : Initial version.

TODO
=====
10. Save clustering results into a matlab/octave readable variable files.
13. Remove simulated parallel processing that uses Collections.Shuffle and paralellize the propagation using a Map/Reduce model.
20. Save plugin settings using the Settings API.
25. [BLOCKED] Upgrade to Gephi 0.9+. (Blocker details: No clustering module/api as of v1.0 of this plugin)

Notes
======
The code bundle comes with matlab/octave helper tools to convert adjacency matrix cell-array based tensor representation of the multidimensional networks' data into a compatible gephi CSV node/edge list files.
The data can thus be imported from the Data Laboratory module of the Gephi software.

Also note that the currently implementation of the plugin/conversion functions uses the label column to display the connecting dimensions sets D(v,u) as a comma-separated string representation 'd1,..,dk'.
This is because we want to show the connecting dimensions along the edge for better readability and visualization.
You may want to use any custom column in the generation tools, but you'll need to update the code of the plugins accordingly.

For test purposes, the network in Figure 1 of the paper [1] was included as a demo data in 3 different formats :
- CSV of node/edge lists that is ready to import from the data laboratory.
- Matlab/octave data variable holding a cell array of adjacency matrices of the individual network's layers.
- .Gephi network file.

The code is freely available for academic and research use. To cite, use the following publication: 

Publications
=============
[1] Boutemine, O., & Bouguessa, M. (2017). Mining Community Structures in Multidimensional Networks. ACM Transactions on Knowledge Discovery from Data (TKDD), 11(4), 51.
