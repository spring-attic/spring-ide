/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;

import org.springframework.beans.factory.BeanFactory;

import test.beans.INestedTestBean;
import test.beans.ITestBean;
import test.beans.TestBean;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class CommonAnnotationBeanPostProcessorTests {
	
	public static class AnnotatedInitDestroyBean {

		public boolean initCalled = false;

		public boolean destroyCalled = false;

		@PostConstruct
		private void init() {
			if (this.initCalled) {
				throw new IllegalStateException("Already called");
			}
			this.initCalled = true;
		}

		@PreDestroy
		private void destroy() {
			if (this.destroyCalled) {
				throw new IllegalStateException("Already called");
			}
			this.destroyCalled = true;
		}
	}

	public static class ResourceInjectionBean extends AnnotatedInitDestroyBean {

		public boolean init2Called = false;

		public boolean init3Called = false;

		public boolean destroy2Called = false;

		public boolean destroy3Called = false;

		@Resource
		private TestBean testBean;

		private TestBean testBean2;

		@PostConstruct
		protected void init2() {
			if (this.testBean == null || this.testBean2 == null) {
				throw new IllegalStateException("Resources not injected");
			}
			if (this.init2Called) {
				throw new IllegalStateException("Already called");
			}
			this.init2Called = true;
		}

		@PostConstruct
		private void init() {
			if (this.init3Called) {
				throw new IllegalStateException("Already called");
			}
			this.init3Called = true;
		}

		@PreDestroy
		protected void destroy2() {
			if (this.destroy2Called) {
				throw new IllegalStateException("Already called");
			}
			this.destroy2Called = true;
		}

		@PreDestroy
		private void destroy() {
			if (this.destroy3Called) {
				throw new IllegalStateException("Already called");
			}
			this.destroy3Called = true;
		}

		@Resource
		public void setTestBean2(TestBean testBean2) {
			if (this.testBean2 != null) {
				throw new IllegalStateException("Already called");
			}
			this.testBean2 = testBean2;
		}

		public TestBean getTestBean() {
			return testBean;
		}

		public TestBean getTestBean2() {
			return testBean2;
		}
	}


	public static class ExtendedResourceInjectionBean extends ResourceInjectionBean {

		@Resource(name="testBean4", type=TestBean.class)
		protected ITestBean testBean3;

		private ITestBean testBean4;

		@Resource
		private INestedTestBean testBean5;

		private INestedTestBean testBean6;

		@Resource
		private BeanFactory beanFactory;

		@Resource
		public void setTestBean2(TestBean testBean2) {
			super.setTestBean2(testBean2);
		}

		@Resource(name="testBean3", type=ITestBean.class)
		private void setTestBean4(ITestBean testBean4) {
			this.testBean4 = testBean4;
		}

		@Resource
		public void setTestBean6(INestedTestBean testBean6) {
			this.testBean6 = testBean6;
		}

		public ITestBean getTestBean3() {
			return testBean3;
		}

		public ITestBean getTestBean4() {
			return testBean4;
		}

		@PostConstruct
		protected void init2() {
			if (this.testBean3 == null || this.testBean4 == null) {
				throw new IllegalStateException("Resources not injected");
			}
			super.init2();
		}

		@PreDestroy
		protected void destroy2() {
			super.destroy2();
		}
	}


	public static class ExtendedEjbInjectionBean extends ResourceInjectionBean {

		@EJB(name="testBean4", beanInterface=TestBean.class)
		protected ITestBean testBean3;

		private ITestBean testBean4;

		@EJB
		private INestedTestBean testBean5;

		private INestedTestBean testBean6;

		@Resource
		private BeanFactory beanFactory;

		@EJB
		public void setTestBean2(TestBean testBean2) {
			super.setTestBean2(testBean2);
		}

		@EJB(beanName="testBean3", beanInterface=ITestBean.class)
		private void setTestBean4(ITestBean testBean4) {
			this.testBean4 = testBean4;
		}

		@EJB
		public void setTestBean6(INestedTestBean testBean6) {
			this.testBean6 = testBean6;
		}

		public ITestBean getTestBean3() {
			return testBean3;
		}

		public ITestBean getTestBean4() {
			return testBean4;
		}

		@PostConstruct
		protected void init2() {
			if (this.testBean3 == null || this.testBean4 == null) {
				throw new IllegalStateException("Resources not injected");
			}
			super.init2();
		}

		@PreDestroy
		protected void destroy2() {
			super.destroy2();
		}
	}


	private static class NamedResourceInjectionBean {

		@Resource(name="testBean9")
		private INestedTestBean testBean;
	}

}
