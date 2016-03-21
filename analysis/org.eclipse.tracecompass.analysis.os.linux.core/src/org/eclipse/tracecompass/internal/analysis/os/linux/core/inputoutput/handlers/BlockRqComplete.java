/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.handlers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.DiskWriteModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.InputOutputStateProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.Request;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers.KernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Request completed event handler
 *
 * @author Houssem Daoud
 */
public class BlockRqComplete extends KernelEventHandler {

    private final InputOutputStateProvider fStateProvider;

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     * @param sp
     *            The state provider calling this handler
     */
    public BlockRqComplete(IKernelAnalysisEventLayout layout, InputOutputStateProvider sp) {
        super(layout);
        fStateProvider = sp;
    }

    @Override
    public void handleEvent(@NonNull ITmfStateSystemBuilder ss, @NonNull ITmfEvent event) throws AttributeNotFoundException {
        ITmfEventField content = event.getContent();
        long ts = event.getTimestamp().getValue();

        Long sector = NonNullUtils.checkNotNull((Long) content.getField(getLayout().fieldBlockSector()).getValue());
        int nrSector = ((Long) content.getField(getLayout().fieldBlockNrSector()).getValue()).intValue();
        int phydisk = ((Long) content.getField(getLayout().fieldBlockDeviceId()).getValue()).intValue();
        int rwbs = ((Long) content.getField(getLayout().fieldBlockRwbs()).getValue()).intValue();
        DiskWriteModel disk = fStateProvider.getDisk(phydisk);

        Request request = disk.getDriverRequest(sector);
        if (request == null) {
            request = new Request(disk, sector, rwbs);
        }
        request.setNrSector(nrSector);
        request.setType(rwbs);
        disk.completeRequest(ts, request);
    }

}
