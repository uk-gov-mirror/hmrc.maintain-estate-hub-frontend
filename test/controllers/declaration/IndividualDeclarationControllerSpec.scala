/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.declaration

import base.SpecBase
import forms.declaration.IndividualDeclarationFormProvider
import models.declaration.IndividualDeclaration
import models.requests.OrganisationUser
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DeclarationService, FakeDeclarationService, FakeFailingDeclarationService}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import views.html.declaration.IndividualDeclarationView

class IndividualDeclarationControllerSpec extends SpecBase {

  val formProvider = new IndividualDeclarationFormProvider()
  val form: Form[IndividualDeclaration] = formProvider()

  lazy val onSubmit: Call = routes.IndividualDeclarationController.onSubmit()

  "Individual Declaration Controller" when {

    "return OK and the correct view for a onPageLoad" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.IndividualDeclarationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[IndividualDeclarationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, declarationEmailEnabled = false)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, routes.IndividualDeclarationController.onPageLoad().url)
          .withFormUrlEncodedBody(("firstName", ""), ("lastName", ""))

      val boundForm = form.bind(Map("firstName" -> "", "lastName" -> ""))

      val view = application.injector.instanceOf[IndividualDeclarationView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, declarationEmailEnabled = false)(request, messages).toString

      application.stop()
    }

    "redirect to confirmation for a POST" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated"
      )))

      val application =
        applicationBuilderForUser(
          userAnswers = Some(emptyUserAnswers),
          user = OrganisationUser("internal", enrolments)
        ).overrides(
          bind[DeclarationService].to(new FakeDeclarationService())
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.confirmation.routes.ConfirmationController.onPageLoad().url

      application.stop()
    }

    "render problem declaring when error retrieving TVN" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment("HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated")))

      val application =
        applicationBuilderForUser(
          userAnswers = Some(emptyUserAnswers),
          user = OrganisationUser("internal", enrolments)
        ).overrides(
          bind[DeclarationService].to(new FakeFailingDeclarationService())
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"))

      val result = route(application, request).value

      redirectLocation(result).value mustBe controllers.declaration.routes.ProblemDeclaringController.onPageLoad().url

      application.stop()
    }
  }

}
