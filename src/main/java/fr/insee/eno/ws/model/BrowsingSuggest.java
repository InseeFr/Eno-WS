package fr.insee.eno.ws.model;

import fr.insee.eno.parameters.BrowsingEnum;

public enum BrowsingSuggest {
	TEMPLATE(BrowsingEnum.TEMPLATE),
	MODULE (BrowsingEnum.MODULE),
	NO_NUMBER(BrowsingEnum.NO_NUMBER);
	
	private BrowsingEnum browsingEnum;

	BrowsingSuggest(BrowsingEnum browsingEnum) {
		this.browsingEnum = browsingEnum ;
	}

	public BrowsingEnum toBrowsingEnum() {
		return browsingEnum;
	}
}
