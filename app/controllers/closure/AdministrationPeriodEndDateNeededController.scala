/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.closure

import com.google.inject.Inject
import controllers.actions._
import pages.WhatIsNextPage
import pages.closure.HasAdministrationPeriodEndedYesNoPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.closure.AdministrationPeriodEndDateNeededView

import scala.concurrent.{ExecutionContext, Future}

class AdministrationPeriodEndDateNeededController @Inject()(
                                                             sessionRepository: SessionRepository,
                                                             actions: Actions,
                                                             val controllerComponents: MessagesControllerComponents,
                                                             view: AdministrationPeriodEndDateNeededView
                                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.authenticatedForUtr {
    implicit request =>

      Ok(view())
  }

  def cleanupAndRedirect(): Action[AnyContent] = actions.authenticatedForUtr.async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers
          .remove(WhatIsNextPage)
          .flatMap(_.remove(HasAdministrationPeriodEndedYesNoPage))
        )
        _ <- sessionRepository.set(updatedAnswers)
      } yield {
        Redirect(controllers.routes.WhatIsNextController.onPageLoad())
      }
  }

}
