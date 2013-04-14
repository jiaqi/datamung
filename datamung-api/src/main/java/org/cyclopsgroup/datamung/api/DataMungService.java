package org.cyclopsgroup.datamung.api;

import org.cyclopsgroup.datamung.api.types.ExportHandler;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;

public interface DataMungService {
	ExportHandler exportInstance(ExportInstanceRequest request);
}
