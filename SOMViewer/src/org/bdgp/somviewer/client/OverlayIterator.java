package org.bdgp.somviewer.client;

import java.util.Iterator;

import org.vaadin.gwtgraphics.client.Group;

public interface OverlayIterator extends Iterator<SOMData.IterateCoordinates> {

	public Group getGroup();
	
	public void setGroup(Group grp);
	
	public String color();
	
	boolean isActive();

}
