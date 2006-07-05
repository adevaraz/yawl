/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package au.edu.qut.yawl.editor.actions.element;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.elements.model.YAWLMultipleInstanceTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

import au.edu.qut.yawl.editor.swing.ActionAndFocusListener;
import au.edu.qut.yawl.editor.swing.JXQueryPanel;
import au.edu.qut.yawl.editor.swing.JXQueryEditorPane;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.net.NetVariableComboBox;
import au.edu.qut.yawl.editor.swing.data.TaskDataVariableUpdateDialog;
import au.edu.qut.yawl.editor.swing.element.TaskVariableComboBox;

import au.edu.qut.yawl.editor.swing.element.AbstractTaskDoneDialog;
import au.edu.qut.yawl.editor.swing.element.MultipleInstanceBoundsPanel;
import au.edu.qut.yawl.editor.swing.data.DataVariableUpdateDialogFactory;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.LinkedList;
  
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class SetMultipleInstanceDetailAction extends YAWLSelectedNetAction {

  private static final InstanceDetailDialog netDialog = new InstanceDetailDialog();

  private NetGraph graph;
  private YAWLMultipleInstanceTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Specify instance detail for this task.  ");
    putValue(Action.NAME, "Set Instance Detail...");
    putValue(Action.LONG_DESCRIPTION, "Specify instance detail for this task.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_I));
  }
  
  public SetMultipleInstanceDetailAction(YAWLMultipleInstanceTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {

    netDialog.setTask(task, graph );
    netDialog.setVisible(true);

    graph.clearSelection();
  }
}

class InstanceDetailDialog extends AbstractTaskDoneDialog {
  
  private MultipleInstanceBoundsPanel boundsPanel = new MultipleInstanceBoundsPanel();
  private MultipleInstanceInstanceParamaterPanel parameterPanel 
  = new MultipleInstanceInstanceParamaterPanel(this);
  
  public InstanceDetailDialog() {
    super(null, true, true);
    setContentPanel(getTabbedPanePanel());
  }
  
  public String getTitleSuffix() {
    return super.getTitleSuffix() + " - Instance Detail";
  }
  
  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  public YAWLMultipleInstanceTask getMultipleInstanceTask() {
    return (YAWLMultipleInstanceTask) getTask();
  }
  
  public void setTask(YAWLMultipleInstanceTask task, NetGraph graph) {
    super.setTask((YAWLTask) task, graph);
    boundsPanel.setTask(task);
    parameterPanel.setTask(task, graph);

    setResizable(true);
    pack();
    setResizable(false);
  }
  
  private JPanel getTabbedPanePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5,5,0,5));
    
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Bounds", boundsPanel);
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_B);
    tabbedPane.addTab("Queries", parameterPanel);
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_Q);

    panel.add(tabbedPane, BorderLayout.CENTER);
    return panel;
  }
}

class MultipleInstanceInstanceParamaterPanel extends JPanel {
  
  private JXQueryPanel accessorQueryPanel; 
  private JXQueryPanel splitterQueryPanel; 
  private JXQueryPanel instanceQueryPanel; 
  private JXQueryPanel aggregateQueryPanel; 
  
  private YAWLMultipleInstanceTask task;
  private NetGraph                 graph;
  
  private AbstractTaskDoneDialog dialog;
  
  private NetVariableComboBox  netVariableBox;
  private TaskVariableComboBox taskVariableBox;

  private JButton createTaskVariableButton;
  private JButton createNetVariableButton;
  
  private final TaskDataVariableUpdateDialog taskVariableUpdateDialog;
  private final TaskDataVariableUpdateDialog netVariableUpdateDialog;
  
  public MultipleInstanceInstanceParamaterPanel(AbstractTaskDoneDialog dialog) {
    super();
    this.dialog = dialog;
    taskVariableUpdateDialog = DataVariableUpdateDialogFactory.getTaskDialog(dialog);
    netVariableUpdateDialog  = DataVariableUpdateDialogFactory.getNetDialog(dialog);

    buildContent();

    getDoneButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          task.setAccessorQuery(accessorQueryPanel.getEditor().getText());
          task.setSplitterQuery(splitterQueryPanel.getEditor().getText());
          task.setInstanceQuery(instanceQueryPanel.getEditor().getText());
          task.setAggregateQuery(aggregateQueryPanel.getEditor().getText());
          
          if (taskVariableBox.getItemCount() > 0) {
            task.setMultipleInstanceVariable(
                taskVariableBox.getSelectedVariable() 
            );
          }

          if (netVariableBox.getItemCount() > 0) {
            task.setResultNetVariable(
                netVariableBox.getSelectedVariable() 
            );
          }
          
          taskVariableBox.setEnabled(false);
          netVariableBox.setEnabled(false);
        }
      }
    );
  }
  
  public void setTask(final YAWLMultipleInstanceTask task, NetGraph graph) {
    this.task  = task;
    this.graph = graph;
    
    setContent();
  }
  
  private void setContent() {
    
    taskVariableBox.setEnabled(false);
    netVariableBox.setEnabled(false);
    
    taskVariableBox.setTask((YAWLTask) task);
    if (task.getMultipleInstanceVariable() != null) {
      taskVariableBox.setSelectedItem(task.getMultipleInstanceVariable().getName());
    } 

    if (taskVariableBox.getItemCount() > 0) {
      taskVariableBox.setEnabled(true);
      if (task.getMultipleInstanceVariable() == null) {
        taskVariableBox.setSelectedIndex(0);
      } 
    }
    
    accessorQueryPanel.getEditor().setText(task.getAccessorQuery());
    splitterQueryPanel.getEditor().setText(task.getSplitterQuery());
    instanceQueryPanel.getEditor().setText(task.getInstanceQuery());
    aggregateQueryPanel.getEditor().setText(task.getAggregateQuery());
    
    netVariableBox.setNet(graph);

    if (task.getResultNetVariable() != null) {
      netVariableBox.setSelectedItem(task.getResultNetVariable().getName());
    }     

    if (netVariableBox.getItemCount() > 0) {
      netVariableBox.setEnabled(true);
      if (task.getResultNetVariable() == null) {
        netVariableBox.setSelectedIndex(0);
      } 
    }
    
    LinkedList comboBoxList = new LinkedList();
    comboBoxList.add(taskVariableBox);
    comboBoxList.add(netVariableBox);
    
    JUtilities.equalizeComponentSizes(comboBoxList);
  }
  
  private void buildContent() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0.333;
    gbc.insets = new Insets(5,5,5,5);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    
    JLabel nameLabel = new JLabel("Multiple Instance Variable:");
    nameLabel.setHorizontalAlignment(JLabel.RIGHT);
    nameLabel.setDisplayedMnemonic('V');
    
    add(nameLabel, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(5,0,5,5);
    gbc.anchor = GridBagConstraints.WEST;
    
    add(getTaskVariableComboBox(),gbc);
    nameLabel.setLabelFor(taskVariableBox);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    add(getTaskVariableCreateButton(),gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 3;
    gbc.weightx = 1;
    gbc.weighty = 0.25;
    gbc.insets = new Insets(0,5,5,5);
    gbc.ipady = 25;
    gbc.fill = GridBagConstraints.BOTH;

    add(getAccessorQueryEditor(),gbc);
    
    gbc.gridy++;
    
    add(getSplitterQueryEditor(),gbc);

    gbc.gridy++;
    
    add(getInstanceQueryEditor(),gbc);

    gbc.gridy++;
    
    add(getAggregateQueryEditor(),gbc);
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weighty = 0;
    gbc.weightx = 0.333;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.ipady = 0;
    gbc.insets = new Insets(5,5,5,5);
    
    JLabel resultLabel = new JLabel("Result Net Variable:");
    resultLabel.setHorizontalAlignment(JLabel.RIGHT);
    resultLabel.setDisplayedMnemonic('R');
    
    add(resultLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;
    
    add(getNetVariableComboBox(),gbc);
    resultLabel.setLabelFor(netVariableBox);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    add(getNetVariableCreateButton(),gbc);
  }
  
  private TaskVariableComboBox getTaskVariableComboBox() {
    taskVariableBox = new TaskVariableComboBox();
    taskVariableBox.setEnabled(false);
  
    new ActionAndFocusListener(taskVariableBox) {
      protected void process(Object eventSource) {
        TaskVariableComboBox thisBox = (TaskVariableComboBox) eventSource;
        if (thisBox.isEnabled()) {
          task.setMultipleInstanceVariable(
            thisBox.getSelectedVariable() 
          );
          accessorQueryPanel.getEditor().setText(
              task.getAccessorQuery()
          );
        }
      }
    };

    return taskVariableBox;
  }

  
  private JXQueryPanel getAccessorQueryEditor() {
    accessorQueryPanel = new JXQueryPanel("Accessor Query");
    
    new ActionAndFocusListener(accessorQueryPanel.getEditor()) {
      protected void process(Object eventSource) {
        JXQueryEditorPane thisEditor = (JXQueryEditorPane) eventSource;
        task.setAccessorQuery(thisEditor.getText());
      }
    };

    return accessorQueryPanel;
  }

  private JXQueryPanel getSplitterQueryEditor() {
    splitterQueryPanel = new JXQueryPanel("Splitter Query");

    new ActionAndFocusListener(splitterQueryPanel.getEditor()) {
      protected void process(Object eventSource) {
        JXQueryEditorPane thisEditor = (JXQueryEditorPane) eventSource;
        task.setSplitterQuery(thisEditor.getText());
      }
    };
    
    return splitterQueryPanel;
  }

  private JXQueryPanel getInstanceQueryEditor() {
    instanceQueryPanel = new JXQueryPanel("Instance Query");

    new ActionAndFocusListener(instanceQueryPanel.getEditor()) {
      protected void process(Object eventSource) {
        JXQueryEditorPane thisEditor = (JXQueryEditorPane) eventSource;
        task.setInstanceQuery(thisEditor.getText());
      }
    };
    
    return instanceQueryPanel;
  }

  private JXQueryPanel getAggregateQueryEditor() {
    aggregateQueryPanel = new JXQueryPanel("Aggregate Query");

    new ActionAndFocusListener(aggregateQueryPanel.getEditor()) {
      protected void process(Object eventSource) {
        JXQueryEditorPane thisEditor = (JXQueryEditorPane) eventSource;
        task.setAggregateQuery(thisEditor.getText());
      }
    };
    
    return aggregateQueryPanel;
  }
  
  private NetVariableComboBox getNetVariableComboBox() {
    netVariableBox = new NetVariableComboBox();
    netVariableBox.setEnabled(false);
  
    new ActionAndFocusListener(netVariableBox) {
      protected void process(Object eventSource) {
        NetVariableComboBox thisBox = (NetVariableComboBox) eventSource;
        if (thisBox.isEnabled()) {
          task.setResultNetVariable(
            thisBox.getSelectedVariable() 
          );
          instanceQueryPanel.getEditor().setText(
              task.getInstanceQuery()
          );
        }
      }
    };
    return netVariableBox;
  }
  
  private JButton getTaskVariableCreateButton() {
    createTaskVariableButton = new JButton("Create...");
    createTaskVariableButton.setMnemonic(KeyEvent.VK_C);

    createTaskVariableButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        DataVariable variable = new DataVariable();
        ((YAWLTask)task).getDecomposition().addVariable(variable);
        taskVariableUpdateDialog.setVariable(variable);
        taskVariableUpdateDialog.setVisible(true);

        if (taskVariableUpdateDialog.cancelButtonSelected()) {
          ((YAWLTask)task).getDecomposition().removeVariable(variable);
        } else {
          task.setMultipleInstanceVariable(variable);
          setContent();
        }
      }
    });

    return createTaskVariableButton; 
  }

  private JButton getNetVariableCreateButton() {
    createNetVariableButton = new JButton("Create...");
    createNetVariableButton.setMnemonic(KeyEvent.VK_E);
    createNetVariableButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        DataVariable variable = new DataVariable();
        graph.getNetModel().getDecomposition().addVariable(variable);
        netVariableUpdateDialog.setVariable(variable);
        netVariableUpdateDialog.setVisible(true);
        
        if (netVariableUpdateDialog.cancelButtonSelected()) {
          graph.getNetModel().getDecomposition().removeVariable(variable);
        } else {
          task.setResultNetVariable(variable);
          setContent();
        }
      }
    });

    return createNetVariableButton; 
  }

  private JButton getDoneButton() {
    return dialog.getDoneButton();
  }
}
