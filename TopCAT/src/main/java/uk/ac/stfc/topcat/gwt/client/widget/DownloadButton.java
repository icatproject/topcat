package uk.ac.stfc.topcat.gwt.client.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;
import uk.ac.stfc.topcat.gwt.shared.Utils;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A download button component. When the download button is pressed the user is
 * presented with a prompt box into which they can enter a name for the
 * download.
 *
 * Fires event Select(downloadName)
 *
 * <dl>
 * <dt><b>Events:</b></dt>
 *
 * <dd><b>Select</b> : DownloadEvent(button, item)<br>
 * <div>Fires after a download name has been entered.</div>
 * <ul>
 * <li>button : this</li>
 * <li>downloadName : the name of the download</li>
 * </ul>
 * </dd>
 *
 * </dl>
 *
 */
public class DownloadButton extends Button {
    private TreePanel<ICATNode> tree = null;
    private CheckBoxSelectionModel<DatafileModel> datafileSelection = null;
    private CheckBoxSelectionModel<DatasetModel> datasetSelection = null;
    private TopcatInvestigation investigation = null;

    public DownloadButton(TreePanel<ICATNode> tree) {
        super(" Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload()));
        this.tree = tree;
        init();
    }

    public DownloadButton(CheckBoxSelectionModel<DatafileModel> checkBoxSelection) {
        super(" Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload()));
        this.datafileSelection = checkBoxSelection;
        init();
    }

    public DownloadButton(String buttonName, AbstractImagePrototype icon, TopcatInvestigation investigation) {
        super(buttonName, icon);
        this.investigation = investigation;
        init();
    }


    @SuppressWarnings("unchecked")
    public DownloadButton(String buttonName, AbstractImagePrototype icon, ICATNodeType type, CheckBoxSelectionModel<?> selection) {
        super(buttonName, icon);


        if (type == ICATNodeType.DATAFILE) {
            this.datafileSelection = (CheckBoxSelectionModel<DatafileModel>) selection;
        }

        if (type == ICATNodeType.DATASET) {
            this.datasetSelection = (CheckBoxSelectionModel<DatasetModel>) selection;
        }

        init();
    }




    private void init() {
        this.addListener(Events.BeforeSelect, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                // prevent recursion
                if (be instanceof DownloadButtonEvent) {
                    return;
                }
                be.setCancelled(true);

                List<ICATNode> selectedItems = getSelectedNodes();

                if (selectedItems.isEmpty()) {
                    EventPipeLine.getInstance().showErrorDialog("Nothing selected for download");
                } else {
                    //check if only one item selected to determine file name
                    if (selectedItems.size() == 1) {
                        //get the first item
                        ICATNode node = selectedItems.get(0);

                        DownloadButtonEvent de = createDownloadButtonEvent();
                        //set download name as first item filename
                        if (node.getNodeType() == ICATNodeType.DATAFILE) {
                            de.setDownloadName(node.getDatafileName());
                        } else if(node.getNodeType() == ICATNodeType.DATASET) {
                            de.setDownloadName(Utils.normaliseFileName(node.getDatasetName()));
                        } else if (node.getNodeType() == ICATNodeType.INVESTIGATION) {
                            de.setDownloadName(Utils.normaliseFileName(node.getInvestigationName()));
                        }

                        fireEvent(Events.Select, de);
                    } else {
                        final MessageBox box = MessageBox.prompt("Download ", "Please enter a name for your download:");
                        //get a suggested filename
                        String suggestedFilename = getFileNameAsDate();
                        box.getTextBox().setValue(suggestedFilename);
                        // listen for a button being pressed
                        box.addCallback(new Listener<MessageBoxEvent>() {
                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                    if (be.getButtonClicked() != null
                                            && be.getButtonClicked().getHtml().equalsIgnoreCase(MessageBox.OK)) {
                                        DownloadButtonEvent de = createDownloadButtonEvent();
                                        de.setDownloadName(be.getValue());
                                        fireEvent(Events.Select, de);
                                    }
                                }

                        });
                        // listen for the enter key in the text box
                        box.getTextBox().addKeyListener(new KeyListener() {
                            @Override
                            public void componentKeyPress(ComponentEvent ce) {
                                if (ce.getKeyCode() == KeyCodes.KEY_ENTER) {
                                    DownloadButtonEvent de = createDownloadButtonEvent();
                                    de.setDownloadName(box.getTextBox().getValue());
                                    fireEvent(Events.Select, de);
                                    box.close();
                                }
                            }
                        });
                    }

                }

            }
        });
    }

    private DownloadButtonEvent createDownloadButtonEvent() {
        return new DownloadButtonEvent(this);
    }

    public TreePanel<ICATNode> getTree() {
        return tree;
    }

    public void setTree(TreePanel<ICATNode> tree) {
        this.tree = tree;
    }


    /**
     * return a normalised date filename
     *
     * @return
     */
    private String getFileNameAsDate() {
        return Utils.normaliseFileName(DateTimeFormat.getFormat("yyyy-MM-dd HH-mm-ss").format(new Date()));
    }

    private List<ICATNode> getSelectedNodes() {
        if (tree != null) {
            return tree.getCheckedSelection();
        }

        if (datafileSelection != null) {
            return convertDatafileModelToICATNode(datafileSelection);
        }

        if (datasetSelection != null) {
            return convertDatasetModelToICATNode(datasetSelection);
        }

        if (investigation != null) {
            return convertInvestigationToICATNode(investigation);
        }

        //return ICATNode array
        List<ICATNode> selectedItems = new ArrayList<ICATNode>();
        return selectedItems;
    }


    private List<ICATNode> convertDatafileModelToICATNode(CheckBoxSelectionModel<DatafileModel> datafileSelectionModel) {
        List<ICATNode> nodes = new ArrayList<ICATNode>();
        // we only interest in the first 2 in the list to determine the download filename
        int count = 0;
        for (DatafileModel datafileModel : datafileSelectionModel.getSelectedItems()) {
            ICATNode iCATNode = new ICATNode();
            iCATNode.setNode(ICATNodeType.DATAFILE, datafileModel.getId(), datafileModel.getName(), datafileModel.getName());
            nodes.add(iCATNode);

            if (count == 2) {
                break;
            }

            count = count + 1;
        }

        return nodes;
    }


    private List<ICATNode> convertInvestigationToICATNode(TopcatInvestigation investigation) {
        List<ICATNode> nodes = new ArrayList<ICATNode>();

        ICATNode iCATNode = new ICATNode();
        iCATNode.setNode(ICATNodeType.INVESTIGATION, investigation.getInvestigationId(), investigation.getInvestigationName(), investigation.getInvestigationName());
        nodes.add(iCATNode);

        return nodes;
    }


    private List<ICATNode> convertDatasetModelToICATNode(CheckBoxSelectionModel<DatasetModel> datasetSelectionModel) {
        List<ICATNode> nodes = new ArrayList<ICATNode>();
        // we only interest in the first 2 in the list to determine the download filename
        int count = 0;
        for (DatasetModel datasetModel : datasetSelectionModel.getSelectedItems()) {
            ICATNode iCATNode = new ICATNode();
            iCATNode.setNode(ICATNodeType.DATASET, datasetModel.getId(), datasetModel.getName(), datasetModel.getName());
            nodes.add(iCATNode);

            if (count == 2) {
                break;
            }

            count = count + 1;
        }

        return nodes;
    }

    public TopcatInvestigation getInvestigation() {
        return investigation;
    }

    public void setInvestigation(TopcatInvestigation investigation) {
        this.investigation = investigation;
    }

    public CheckBoxSelectionModel<DatafileModel> getDatafileSelection() {
        return datafileSelection;
    }

    public void setDatafileSelection(
            CheckBoxSelectionModel<DatafileModel> datafileSelection) {
        this.datafileSelection = datafileSelection;
    }

    public CheckBoxSelectionModel<DatasetModel> getDatasetSelection() {
        return datasetSelection;
    }

    public void setDatasetSelection(CheckBoxSelectionModel<DatasetModel> datasetSelection) {
        this.datasetSelection = datasetSelection;
    }


}
