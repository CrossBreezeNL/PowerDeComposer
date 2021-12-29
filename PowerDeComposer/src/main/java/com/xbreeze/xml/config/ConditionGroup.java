package com.xbreeze.xml.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({AllConditionGroup.class, OneOffConditionGroup.class})
public abstract class ConditionGroup implements ElementConditionOrGroup {
	@XmlElements({
		@XmlElement(name = "AllConditions", type=AllConditionGroup.class),
		@XmlElement(name = "OneOffConditions", type=OneOffConditionGroup.class),
		@XmlElement(name = "ElementCondition", type=ElementCondition.class)
	})
	private List<? extends ElementConditionOrGroup> _elementConditionsAndGroups;

	public List<? extends ElementConditionOrGroup> getElementConditionsAndGroups() {
		return _elementConditionsAndGroups;
	}

	public void setElementConditionsAndGroups(List<ElementConditionOrGroup> elementConditionsAndGroups) {
		this._elementConditionsAndGroups = elementConditionsAndGroups;
	}
	
	@Override
	public String getXPathExpression() {
		StringBuffer stringBuffer = new StringBuffer();
		for (int idx=0; idx < this.getElementConditionsAndGroups().size(); idx++) {
			// If this is not the first condition, add the and keyword.
			if (idx != 0) {
				stringBuffer.append(" ");
				stringBuffer.append(this.getXPathConditionOperator());
				stringBuffer.append(" ");
			}
			stringBuffer.append("(");
			stringBuffer.append(this.getElementConditionsAndGroups().get(idx).getXPathExpression());
			stringBuffer.append(")");
		}
		return stringBuffer.toString();
	}
	
	protected abstract String getXPathConditionOperator();
}
