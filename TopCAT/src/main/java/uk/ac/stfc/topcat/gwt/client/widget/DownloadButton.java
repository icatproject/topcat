package uk.ac.stfc.topcat.gwt.client.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;
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
    private CheckBoxSelectionModel<DatafileModel> checkBoxSelection = null;
    
    public DownloadButton(TreePanel<ICATNode> tree) {
        super(" Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload()));
        this.tree = tree;
        init();
    }
    
    public DownloadButton(CheckBoxSelectionModel<DatafileModel> checkBoxSelection) {
        super(" Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload()));
        this.checkBoxSelection = checkBoxSelection;
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
                        de.setDownloadName(node.getDatafileName());
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
        
        if (checkBoxSelection != null) {
            return convertDatafileModelToICATNode(checkBoxSelection);
            
        }
        
        //return ICATNode array
        List<ICATNode> selectedItems = new ArrayList<ICATNode>();
        return selectedItems;
    }
    
    private List<ICATNode> convertDatafileModelToICATNode(CheckBoxSelectionModel<DatafileModel> datafileSelectionModel) {
        List<ICATNode> nodes = new ArrayList<ICATNode>();
        
        for (DatafileModel datafileModel : datafileSelectionModel.getSelectedItems()) {
            ICATNode iCATNode = new ICATNode();
            iCATNode.setNode(ICATNodeType.DATAFILE, datafileModel.getId(), datafileModel.getName(), datafileModel.getName());
            nodes.add(iCATNode);
        }
        
        return nodes;
    }

}
