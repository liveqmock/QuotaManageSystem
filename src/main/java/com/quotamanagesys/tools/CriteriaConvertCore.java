package com.quotamanagesys.tools;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bstek.dorado.data.provider.Criteria;
import com.bstek.dorado.data.provider.Criterion;
import com.bstek.dorado.data.provider.Or;
import com.bstek.dorado.data.provider.filter.SingleValueFilterCriterion;

@Component
public class CriteriaConvertCore {

	public String convertToSQLString(Criteria criteria){
		String filterString="";
		if (criteria!=null) {
			List<Criterion> criterions=criteria.getCriterions();
			for (Criterion criterion : criterions) {
				if (criterion.getClass().getName().equals("com.bstek.dorado.data.provider.Or")) {
					Or or=(Or)criterion;
					Collection<Criterion> orCriterions=or.getCriterions();
					String orFilterString="";
					for (Criterion orCriterion : orCriterions) {
						SingleValueFilterCriterion singleValueFilterCriterion=(SingleValueFilterCriterion)orCriterion;
						String propertyString=singleValueFilterCriterion.getProperty();
						String oparateString=singleValueFilterCriterion.getFilterOperator().toString();
						String valueString=singleValueFilterCriterion.getValue().toString();
						String datatypeString=singleValueFilterCriterion.getDataType().getName();

						if (datatypeString.equals("int")||datatypeString.equals("boolean")||datatypeString.equals("double")
								||datatypeString.equals("float")) {
							if (orFilterString.equals("")) {
								orFilterString="("+propertyString+" "+oparateString+" "+valueString+")";
							} else {
								orFilterString=orFilterString+" or ("+propertyString+" "+oparateString+" "+valueString+")";
							}
						}else {
							if (orFilterString.equals("")) {
								if (oparateString.equals("like")||oparateString.equals("not like")) {
									orFilterString="("+propertyString+" "+oparateString+" '%"+valueString+"%')";
								}else {
									orFilterString="("+propertyString+" "+oparateString+" '"+valueString+"')";
								}
							} else {
								if (oparateString.equals("like")||oparateString.equals("not like")) {
									orFilterString=orFilterString+" or ("+propertyString+" "+oparateString+" '%"+valueString+"%')";
								}else {
									orFilterString=orFilterString+" or ("+propertyString+" "+oparateString+" '"+valueString+"')";
								}
							}
						}
					}	
					orFilterString="("+orFilterString+")";
					
					if (filterString.equals("")) {
						filterString=orFilterString;
					} else {
						filterString=filterString+" and "+orFilterString;
					}
				}else {
					SingleValueFilterCriterion singleValueFilterCriterion=(SingleValueFilterCriterion)criterion;
					String propertyString=singleValueFilterCriterion.getProperty();
					String oparateString=singleValueFilterCriterion.getFilterOperator().toString();
					String valueString=singleValueFilterCriterion.getValue().toString();
					String datatypeString=singleValueFilterCriterion.getDataType().getName();
					if (datatypeString.equals("int")||datatypeString.equals("boolean")||datatypeString.equals("double")
							||datatypeString.equals("float")) {
						if (filterString.equals("")) {
							filterString="("+propertyString+" "+oparateString+" "+valueString+")";
						} else {
							filterString=filterString+" and ("+propertyString+" "+oparateString+" "+valueString+")";
						}	
					}else {
						if (filterString.equals("")) {
							if (oparateString.equals("like")||oparateString.equals("not like")) {
								filterString="("+propertyString+" "+oparateString+" '%"+valueString+"%')";
							}else {
								filterString="("+propertyString+" "+oparateString+" '"+valueString+"')";
							}
						} else {
							if (oparateString.equals("like")||oparateString.equals("not like")) {
								filterString=filterString+" and ("+propertyString+" "+oparateString+" '%"+valueString+"%')";
							}else {
								filterString=filterString+" and ("+propertyString+" "+oparateString+" '"+valueString+"')";
							}
						}	
					}
				}
			}
		}
		return filterString;
	}
	
}
