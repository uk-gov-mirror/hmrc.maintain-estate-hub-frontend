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

package views

import play.api.Application
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AgentNotAuthorisedView

class AgentNotAuthorisedViewSpec extends ViewBehaviours {

  private val fakeUtr: String = "1234567890"

  private val application: Application = applicationBuilder().build()

  private val view: AgentNotAuthorisedView = application.injector.instanceOf[AgentNotAuthorisedView]

  private val applyView: HtmlFormat.Appendable = view.apply(utr = fakeUtr)(fakeRequest, messages)

  "Agent not authorised view" when {

    behave like pageWithSubHeading(applyView, fakeUtr)

    behave like normalPageTitleWithCaption(applyView,
      "agentNotAuthorised",
      fakeUtr,
      "p1", "p2", "p2.link", "p3", "p4", "p4.link", "p5", "p5.link"
    )
  }
}
