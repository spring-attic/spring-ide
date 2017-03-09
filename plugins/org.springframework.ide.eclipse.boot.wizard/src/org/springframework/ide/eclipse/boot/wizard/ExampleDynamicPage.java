package org.springframework.ide.eclipse.boot.wizard;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ide.eclipse.boot.livexp.ui.DynamicSection;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ExampleDynamicPage extends WizardPageWithSections {

	protected ExampleDynamicPage() {
		super("example", "An Example" , null);
	}

	private static class DynamicModelExample {
		public final StringFieldModel numberAsString = new StringFieldModel("Count", "");
		{
			numberAsString.validator(positiveNumberValidator(numberAsString));
		}

		public final LiveExpression<Integer> number = numberAsString.getVariable().apply((String input) -> {
			try {
				return Integer.parseInt(input);
			} catch (Exception e) {
				return null;
			}
		});


		private final LiveExpression<List<FieldModel<String>>> dynamicModel = new LiveExpression<List<FieldModel<String>>>() {
			{
				dependsOn(number);
			}

			@Override
			protected List<FieldModel<String>> compute() {
				Builder<FieldModel<String>> fields = ImmutableList.builder();
				Integer numberOfFields = number.getValue();
				if (numberOfFields!=null) {
					for (int i = 0; i < numberOfFields; i++) {
						StringFieldModel f = new StringFieldModel("Field"+i, ""+i);
						f.validator(positiveNumberValidator(f));
						fields.add(f);
					}
				}
				return fields.build();
			}
		};
	}

	private DynamicModelExample model = new DynamicModelExample();

	@Override
	protected List<WizardPageSection> createSections() {
		ExampleDynamicPage owner = this;
		return ImmutableList.of(
				(WizardPageSection)new StringFieldSection(owner, model.numberAsString),
				new DynamicSection(this, this.model.dynamicModel.apply((dynamicFields) -> {
					if (dynamicFields!=null && dynamicFields.size()>0) {
						WizardPageSection[] fieldSections = dynamicFields.stream()
						.map((field) -> new StringFieldSection(owner, field))
						.collect(Collectors.toList())
						.toArray(new WizardPageSection[0]);
						return new GroupSection(owner, null, fieldSections);
					}
					return new CommentSection(ExampleDynamicPage.this, "Nothing to show");
				}))
		);
	}

	private static Validator positiveNumberValidator(FieldModel<String> stringField) {
		return new Validator() {
			{
				dependsOn(stringField.getVariable());
			}
			@Override
			protected ValidationResult compute() {
				String string = stringField.getValue();
				if (!StringUtils.hasText(string)) {
					return ValidationResult.error(stringField.getLabel()+": must be a integer");
				}
				try {
					int number = Integer.parseInt(string);
					if (number<=0) {
						return ValidationResult.error(stringField.getLabel()+": must be positive");
					}
				} catch (Exception e) {
					return ValidationResult.error(stringField.getLabel()+": "+ExceptionUtil.getMessage(e));
				}
				return null;
			}
		};
	}

}
