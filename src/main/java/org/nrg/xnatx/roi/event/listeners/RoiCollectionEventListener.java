/*********************************************************************
 * Copyright (c) 2018, Institute of Cancer Research
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * (3) Neither the name of the Institute of Cancer Research nor the
 *     names of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************/
package org.nrg.xnatx.roi.event.listeners;

import org.nrg.xnatx.roi.service.RoiService;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import static reactor.bus.selector.Selectors.R;
import reactor.fn.Consumer;

/**
 *
 * @author jamesd
 */
@Service
public class RoiCollectionEventListener implements Consumer<Event<WorkflowStatusEvent>>
{
	private static final Logger logger = LoggerFactory.getLogger(
		RoiCollectionEventListener.class);

	private final RoiService roiService;

	@Autowired
	public RoiCollectionEventListener(EventBus eventBus, RoiService roiService)
	{
		this.roiService = roiService;
		eventBus.on(R(WorkflowStatusEvent.class.getName()+
				"[.]?("+PersistentWorkflowUtils.COMPLETE+")"),
			this);
	}

	@Override
	public void accept(Event<WorkflowStatusEvent> event)
	{
		WorkflowStatusEvent wfsEvent = event.getData();
		if (wfsEvent.getWorkflow() instanceof WrkWorkflowdata)
		{
			handleEvent(wfsEvent);
		}
	}

	private void handleEvent(WorkflowStatusEvent wfsEvent)
	{
		WrkWorkflowdata workflow = (WrkWorkflowdata) wfsEvent.getWorkflow();
		String pipelineName = workflow.getPipelineName();
		String dataType = workflow.getDataType();
		String experimentId = workflow.getId();
		switch (pipelineName)
		{
			case "Removed ROI Collection":
			{
				onRoiCollectionRemoved(experimentId);
				return;
			}

			default:
		}

//		logger.debug("WorkflowStatusEvent\n  * EntityId: "+wfsEvent.getEntityId()+
//			"\n  * EntityType: "+wfsEvent.getEntityType()+
//			"\n  * Status: "+wfsEvent.getStatus());
//		logger.debug("Workflowdata\n  * PipelineName: "+pipelineName+
//			"\n  * DataType: "+dataType+
//			"\n  * Category: "+workflow.getCategory()+
//			"\n  * ExperimentId: "+experimentId+
//			"\n  * Description: "+workflow.getDescription()+
//			"\n  * Details: "+workflow.getDetails()+
//			"\n  * SchemaElement: "+workflow.getSchemaElementName()+
//			"\n  * Status: "+workflow.getStatus()+
//			"\n  * Id: "+workflow.getId()+
//			"\n  * XsiType: "+workflow.getXSIType());
	}

	private void onRoiCollectionRemoved(String experimentId)
	{
		logger.debug("Event: Removed ROI collection {}", experimentId);
		roiService.deleteCollectionRois(experimentId);
	}
	
}
