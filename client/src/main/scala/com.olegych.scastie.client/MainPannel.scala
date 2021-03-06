package com.olegych.scastie
package client

import japgolly.scalajs.react._, vdom.all._

import org.scalajs.dom.raw.HTMLPreElement

object MainPannel {

  private val consoleElement = Ref[HTMLPreElement]("console")
  private val component =
    ReactComponentB[(AppState, AppBackend, AppProps)]("MainPannel")
      .render_P {
        case (state, backend, props) =>
          def show(view: View) = {
            if (view == state.view) TagMod(display.block)
            else TagMod(display.none)
          }

          val theme = if (state.isDarkTheme) "dark" else "light"

          val consoleCss =
            if (state.consoleIsOpen) "with-console"
            else ""

          val embedded = props.embedded.isDefined

          val embeddedMenu =
            if (embedded) TagMod(EmbeddedMenu(state, backend))
            else EmptyTag

          def toogleShowHelpAtStartup(e: ReactEvent): Callback = {
            backend.toggleHelpAtStartup()
          }

          def closeHelp(e: ReactEvent): Callback = {
            backend.closeHelp()
          }

          val showHelp =
            if (state.isShowingHelpAtStartup && state.isStartup && !embedded)
              true
            else !state.isHelpModalClosed

          val helpClosePannel =
            if (showHelp) {
              TagMod(
                div(`class` := "help-close")(
                  button(onClick ==> closeHelp)("Close"),
                  div(`class` := "not-again")(
                    p("Dont show again"),
                    input.checkbox(onChange ==> toogleShowHelpAtStartup,
                                   checked := !state.isShowingHelpAtStartup)
                  )
                )
              )
            } else EmptyTag

          val helpState =
            if (showHelp) {
              val helpModal =
                api.Instrumentation(api.Position(0, 0),
                                    api.runtime.help.copy(folded = false))

              state.copy(
                outputs = state.outputs.copy(
                  instrumentations = state.outputs.instrumentations + helpModal
                )
              )
            } else state

          import state._

          val debugOutput =
            pre(`class` := "debug")(
              s"""|inputsHasChanged:       $inputsHasChanged
                  |
                  |snippetId:              $snippetId
                  |
                  |isSnippetSaved:         $isSnippetSaved
                  |
                  |loadSnippet:            $loadSnippet
                  |
                  |loadScalaJsScript:      $loadScalaJsScript
                  |
                  |isScalaJsScriptLoaded:  $isScalaJsScriptLoaded
                  |
                  |snippetIdIsScalaJS:     $snippetIdIsScalaJS
                  |
                  |attachedDoms:           $attachedDoms
                  |
                  |outputs
                  |  instrumentations:
                  |    ${outputs.instrumentations.mkString("    \n")}
                  |""".stripMargin
            )

          div(`class` := "main-pannel")(
            // debugOutput,
            TopBar(state, backend),
            div(`class` := s"pannel $theme $consoleCss", show(View.Editor))(
              helpClosePannel,
              Editor(helpState, backend),
              embeddedMenu,
              pre(`class` := "output-console", ref := consoleElement)(
                state.outputs.console
              )
            ),
            div(`class` := s"pannel $theme", show(View.Libraries))(
              Libraries(state, backend)
            ),
            div(`class` := s"pannel $theme", show(View.UserProfile))(
              UserProfile(props.router, state.view)
            )
          )
      }
      .componentDidUpdate(
        scope =>
          Callback {
            consoleElement(scope.$).foreach { consoleDom =>
              consoleDom.scrollTop = consoleDom.scrollHeight.toDouble
            }
        }
      )
      .build

  def apply(state: AppState, backend: AppBackend, props: AppProps) =
    component((state, backend, props))
}
