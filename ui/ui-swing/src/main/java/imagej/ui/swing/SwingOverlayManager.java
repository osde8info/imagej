//
// SwingOverlayManager.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.roi.AbstractOverlay;
import imagej.data.roi.Overlay;
import imagej.display.OverlayManager;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.AbstractListModel;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
 * TODO: select overlays in display as they are selected in the list
 * delete overlays when they are deleted in the list
 * Add mechanism for scrolling in Display to make the selected overlay visible.
 */

/**
 * Overlay Manager Swing UI
 * 
 * @author Adam Fraser
 */
public class SwingOverlayManager extends JFrame implements ActionListener{
	private static final long serialVersionUID = -6498169032123522303L;
	private JList olist = null;

	/*
	 * Constructor. Create a JList to list the overlays. 
	 */
	public SwingOverlayManager() {
		olist = new JList(new OverlayListModel());
		olist.setCellRenderer(new OverlayRenderer());
		
		// Populate the list with the current overlays
		OverlayManager om = ImageJ.get(OverlayManager.class);			
		for (Overlay overlay : om.getOverlays()) {
			olist.add(olist.getCellRenderer().getListCellRendererComponent(olist, overlay, -1, false, false));
		}
		
		JScrollPane listScroller = new JScrollPane(olist);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		JButton delbutton = new JButton("delete selected");
		delbutton.setMnemonic(KeyEvent.VK_DELETE);
		delbutton.setActionCommand("delete");
		delbutton.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(delbutton);
		
		setSize(300, 300);
		
		Container cp = this.getContentPane();
		cp.add(listPane, BorderLayout.CENTER);
		cp.add(buttonPane, BorderLayout.PAGE_END);
		
		//
		// Listen to list selections
		// TODO: update Overlay selection in Displays from this selection
		//
	 	ListSelectionListener listSelectionListener = new ListSelectionListener() {
	 		public void valueChanged(ListSelectionEvent listSelectionEvent) {
	 			System.out.println("First index: " + listSelectionEvent.getFirstIndex());
	 			System.out.println(", Last index: " + listSelectionEvent.getLastIndex());
		        boolean adjust = listSelectionEvent.getValueIsAdjusting();
		        System.out.println(", Adjusting? " + adjust);
		        if (!adjust) {
		        	JList list = (JList) listSelectionEvent.getSource();
		        	int selections[] = list.getSelectedIndices();
		        	Object selectionValues[] = list.getSelectedValues();
		        	for (int i = 0, n = selections.length; i < n; i++) {
		        		if (i == 0) {
		        			System.out.println(" Selections: ");
		        		}
		        		System.out.println(selections[i] + "/" + selectionValues[i] + " ");
		        	}
		        }
	 		}
	 	};
	 	olist.addListSelectionListener(listSelectionListener);
	 	
	}
	
	public void actionPerformed(ActionEvent e){
		if ("delete".equals(e.getActionCommand())) {
			AbstractOverlay overlay = (AbstractOverlay)olist.getSelectedValue();
//			overlay.delete();
			System.out.println("TODO: Delete overlay "+overlay.getRegionOfInterest().toString());
		}
	}

	
	public class OverlayListModel extends AbstractListModel {
		private static final long serialVersionUID = 7941252533859436640L;
		private OverlayManager om;
		private EventSubscriber<OverlayCreatedEvent> overlaycreatedsubscriber = 
			new EventSubscriber<OverlayCreatedEvent>(){
				@Override
				public void onEvent(OverlayCreatedEvent event) {
					System.out.println("\tCREATED: "+event.toString());
					Overlay overlay = event.getObject();
					int index = olist.getComponents().length;
					olist.add(olist.getCellRenderer().getListCellRendererComponent(olist, overlay, index, false, false), 
							index);
					olist.updateUI();
				}
			};
		private EventSubscriber<OverlayDeletedEvent> overlaydeletedsubscriber = 
			new EventSubscriber<OverlayDeletedEvent>(){
				@Override
				public void onEvent(OverlayDeletedEvent event) {
					System.out.println("\tDELETED: "+event.toString());
					Overlay overlay = event.getObject();
					olist.remove(olist.getCellRenderer().getListCellRendererComponent(olist, overlay, -1, false, false));
					olist.updateUI();
				}
			};
		private EventSubscriber<OverlayRestructuredEvent> overlayrestructuredsubscriber =
			new EventSubscriber<OverlayRestructuredEvent>(){
				@Override
				public void onEvent(OverlayRestructuredEvent event) {
					System.out.println("\tRESTRUCTURED: "+event.toString());
					// TODO: update overlay thumbnails
//					Overlay overlay = event.getObject();
//					olist.updateUI();
				}
			};

		/*
		 * Constructor. Bind Overlay events.
		 */
		public OverlayListModel() {
			om = ImageJ.get(OverlayManager.class);			
			Events.subscribe(OverlayCreatedEvent.class, overlaycreatedsubscriber);
			Events.subscribe(OverlayDeletedEvent.class, overlaydeletedsubscriber);
			Events.subscribe(OverlayRestructuredEvent.class, overlayrestructuredsubscriber);
		}

		public Object getElementAt(int index) {
			return om.getOverlays().get(index);
		}

		public int getSize() {
			return om.getOverlays().size();
		}
	}

	
	public class OverlayRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 2468086636364454253L;
		private Hashtable iconTable = new Hashtable();

		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected, boolean hasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, hasFocus);
			if (value instanceof Overlay) {
				Overlay overlay = (Overlay) value;
				//TODO: create overlay thumbnail from overlay 
				ImageIcon icon = (ImageIcon) iconTable.get(value);
//				if (icon == null) {
//					icon = new ImageIcon(...);
//					iconTable.put(value, icon);
//				}
				label.setIcon(icon);
			} else {
				// Clear old icon; needed in 1st release of JDK 1.2
				label.setIcon(null); 
			}
			return label;
		}
	}

}
