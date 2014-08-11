package com.quotamanagesys.interceptor;

public class CalculateParameter {

	private String parameterName;
	private String parameterValue;
	
	public CalculateParameter(String parameterName,String parameterValue){
		this.parameterName=parameterName;
		this.parameterValue=parameterValue;
	}
	
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public String getParameterValue() {
		return parameterValue;
	}
	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}
}
