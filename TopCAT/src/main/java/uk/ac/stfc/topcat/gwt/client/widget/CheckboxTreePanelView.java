package uk.ac.stfc.topcat.gwt.client.widget;

import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelView;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.Joint;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class CheckboxTreePanelView<M extends ModelData> extends TreePanelView<M> {
    @SuppressWarnings({ "incomplete-switch", "unchecked" })
    public String getTemplate(ModelData m, String id, String text, AbstractImagePrototype icon, boolean checkable,
            boolean checked, Joint joint, int level, TreeViewRenderMode renderMode) {
          if (renderMode == TreeViewRenderMode.CONTAINER) {
            if (GXT.isIE6 || GXT.isIE7) {
              return "<div unselectable=on class=\"x-tree3-node-ct\" style=\"position:relative;\" role=\"group\"></div>";
            } else {
              return "<div unselectable=on class=\"x-tree3-node-ct\" style=\"position:relative;\" role=\"group\"><table cellpadding=0 cellspacing=0 width=100%><tr><td></td></tr></table></div>";
            }
          }
          StringBuilder sb = new StringBuilder();
          if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.MAIN) {
            sb.append("<div unselectable=on id=\"");
            sb.append(id);
            sb.append("\"");

            sb.append(" class=\"x-tree3-node\"  role=\"presentation\">");

            String cls = "x-tree3-el";
            if (GXT.isHighContrastMode) {
              switch (joint) {
                case COLLAPSED:
                  cls += " x-tree3-node-joint-collapse";
                  break;
                case EXPANDED:
                  cls += " x-tree3-node-joint-expand";
                  break;
              }
            }

            sb.append("<div unselectable=on class=\"" + cls + "\" id=\"" + tree.getId() + "__" + id + "\" role=\"treeitem\" ");
            sb.append(" aria-level=\"" + (level + 1) + "\">");
          }
          if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.BODY) {
            Element jointElement = null;
            switch (joint) {
              case COLLAPSED:
                jointElement = (Element) tree.getStyle().getJointCollapsedIcon().createElement().cast();
                break;
              case EXPANDED:
                jointElement = (Element) tree.getStyle().getJointExpandedIcon().createElement().cast();
                break;
            }

            if (jointElement != null) {
              El.fly(jointElement).addStyleName("x-tree3-node-joint");
            }

            sb.append("<img src=\"");
            sb.append(GXT.BLANK_IMAGE_URL);
            sb.append("\" style=\"height: 18px; width: ");
            sb.append(level * getIndenting(findNode((M) m)));
            sb.append("px;\" />");
            sb.append(jointElement == null ? "<img src=\"" + GXT.BLANK_IMAGE_URL
                + "\" style=\"width: 16px\" class=\"x-tree3-node-joint\" />" : DOM.toString(jointElement));
            
            //disable checkbox if node is not an investigation, dataset or datafile
            ICATNode node = (ICATNode) m;
            
            boolean showCheckbox = true;
            
            if (node.getNodeType() != ICATNodeType.DATAFILE && node.getNodeType() != ICATNodeType.DATASET
                    && node.getNodeType() != ICATNodeType.INVESTIGATION) {
                showCheckbox = false;
            }            
            
            if (checkable && showCheckbox) {
              Element e = (Element) (checked ? GXT.IMAGES.checked().createElement().cast()
                  : GXT.IMAGES.unchecked().createElement().cast());
              El.fly(e).addStyleName("x-tree3-node-check");
              sb.append(DOM.toString(e));
            } else {
              sb.append("<span class=\"x-tree3-node-check\"></span>");
            }
            if (icon != null) {
              Element e = icon.createElement().cast();
              El.fly(e).addStyleName("x-tree3-node-icon");
              sb.append(DOM.toString(e));
            } else {
              sb.append("<span class=\"x-tree3-node-icon\"></span>");
            }
            sb.append("<span  unselectable=on class=\"x-tree3-node-text\">");
            sb.append(text);
            sb.append("</span>");
          }

          if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.MAIN) {
            sb.append("</div>");
            sb.append("</div>");
          }
          return sb.toString();
        }
    

}
