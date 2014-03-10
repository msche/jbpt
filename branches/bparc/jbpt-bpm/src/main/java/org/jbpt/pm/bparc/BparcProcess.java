package org.jbpt.pm.bparc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jbpt.pm.NonFlowNode;

public class BparcProcess extends NonFlowNode implements IBparcProcess {
	
	List<IEvent> events = new ArrayList<IEvent>();
	private String shapeId;
	
	/**
	 * @param name
	 * @param description
	 */
	public BparcProcess(String name, String resourceId) {
		super(name);
		this.shapeId = resourceId;
	}

	public List<IEvent> getEvents() {
		return events;
	}
	
	public void addEvent(IEvent e) {
		events.add(e);
	}
	
	public void addEvents(List<IEvent> events) {
		events.addAll(events);
	}
	
	public UUID getUid() {
		//TODO: probably unnecessary
		return UUID.fromString(super.getId());
	}
	
	public String getShapeId() {
		return shapeId;
	}
	
}