# SRE Plugin: Debugging SWRL rules in Protégé 

![version](https://img.shields.io/badge/version-0.2.1-blue) ![license](https://img.shields.io/badge/license-GPLv3-purple) ![status](https://img.shields.io/badge/activity%20status-paused-lightgrey)

Protégé in a graphical editor that can be used to model and create OWL ontologies. SRE Plugin extends Protégé with the ability to interactively debug rules expressed in Semantic Web Rule Language (SWRL). For this purpose, the plugin provides implementation of our proposed SRE algorithm. This helps locating logical inconsistencies in the rule base when ontologies increase in size and complexity.

For the citation of SRE Plugin, please refer to our publication:
[Geyer J, Nguyen J, Farrenkopf T, Guckert M. (2018) Single Rule Evaluation (SRE): Computational Algorithmic Debugging for Complex SWRL Rules, IC3K 2018, 10th International Joint Conference on Knowledge Discovery, Knowledge Engineering and Knowledge Management, Seville, Spain](https://www.scitepress.org/Papers/2018/69241/69241.pdf)



## Getting started
To get started with the plugin please follow instructions: 

### 1. Download the plugin:
There are two options to download the plugin. 
You can find the latest version of the plugin within the Protégé Ontology editor using the auto-update function, which checks for available plugins. Older versions of the plugin are be available on this Github Repository (Kite-Cloud/SRE). Under "releases" you will find all published versions which have to be manually added to the "plugins" folder of your Protégé.

### 2. Using the plugin
To open the installed plugin in Protége navigate to: `Window -> Tabs -> SRE-Tab`

The graphical user interface of SRE is integrated into Protége and divided into three areas. The first area is a drop-down menu which can be used to select a specific rule for debugging. To start rule debugging, press the `Evaluate Rule` button.
Debugging reveals values of rule atoms that are computed during reasoning. As computed values are specific to individuals, users need to interactively choose the individuals they intend to study. The canvas area visualises results of the evaluated rule atoms. Rules are visualised as a tree structure using Cajun Visualization Library. To access detailed information on rule atoms click on the relevant node in the tree structure which opens a pop-up window. Finally, the bottom area of the plugin provides an output console containing logs from SRE evaluations. 

![protegeViewer](https://user-images.githubusercontent.com/39269984/117508280-9aa74a80-af88-11eb-82ed-5464d7e7e001.PNG)
