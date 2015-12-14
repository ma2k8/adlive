package adlive.application.controllers

import play.api._
import play.api.mvc._

class Dashboard extends Controller {

  def index = Action {
    Ok(adlive.presentation.html.index("Your new application is ready.",List("streamA","streamB")))
  }

  def create() = TODO

  def update() = TODO

  def delete() = TODO

}
