
# COP Model View Intent  
  
## Objetivo  :dart:
- Hablar menos de arquitectura y practicar más.
- Enfrentarte a lo desconocido. :ghost:
- Rebajar la sensación de complejidad del patrón MVI, es otra f***ing interpretación del patrón MVC. No muerde. :poop: 
- Compartir conocimientos y opiniones con tus compañeros. Hay que hacer piña. :pineapple: **#bettertogether**
- Practicar, practicar y practicar. Learning by doing.

## Descripción :book:
> TL;DR: Listado donde se puede filtrar por el nombre del país y al clicar en un item se navega a una pantalla nueva. El ejercicio consiste en transformar la implementación actual a un diseño Model View Intent

 La arquitectura elegida ha sido "Spaguetti architecture":spaghetti:, se explica por si sola.
 - `SelectCountryActivity.kt`: Toda la magia sucede aquí.
 - `CountriesRepo.kt`: Fuente de datos para los países a mostrar.
 - `LocationManager.kt`: Clase encargada de configurar los valores de país e idioma.
 - `Tracker.kt`: Clase encargada de tracking de eventos de analítica.
 - `Navigator.kt`: Clase encargada de gestionar la navegación entre pantallas. 
 - `AppFactory.kt`: Clase de ayuda para gestionar la dependencias.

## Comportamiento deseado :pray:

El código inicial  cumple los siguientes requerimientos:

- Al ejecutar la app se mostrar el listado los países disponibles(Para simplificar cada país sólo tiene un idioma asociado)

- La *top bar* tiene funcionalidad de filtrado de países por texto.
	- Al clicar en el icono de la lupa se activa el modo búsqueda.
	- Al clicar el icono de la cruz se sale del modo búsqueda
	- Al empezar a escribir en la barra de búsqueda se actualizará el listado de países cuyo nombre contenga el *input* del usuario.
	- Si ningún país cumple con la condición de filtrado se mostrará un texto con mensaje: "Sin resultados"

- Cuando se seleccione un país del listado(clic en la celda):
	- Enviar evento vía **Tracker.kt**
	- Set up de los valores de localización de la usuaria vía **LocationManager.kt**
	
- En caso de algún tipo de error se muestra una pantalla de error con un botón de *Retry*

- **TODO**: Navegación a una nueva pantalla(AKA Home). ¡Hazlo como quieras!

  
  
## Reglas  :straight_ruler: 
¡Ninguna! Puedes cambiar o crear cualquier clase, función... ¡Lo que te dé la gana vamos!  
 
## Preguntas :question:
- ¿Que *Intents* has declarado?
- ¿Has gestionado los cambios del Model con estados?, ¿Qué estados has definido?
- ¿Consideras que el resultado responde a una arquitectura uni-direccional?
- ¿Cómo has gestionado el foco del Edittext y la del teclado?
- ¿Cómo has gestionado el *lifecycle*?
- ¿Cómo organizarías el *packaging* o directorio del proyecto?  
- ¿Has realizado tests unitarios? ¿Has conseguido implementar algún test de UI? :top:
- ¿Cómo de acoplado al framework de Android consideras tu implementación de MVI?
- ¿Has usado el componente `ViewModel` de Android?,¿Por qué?,¿Cuál es su función?
- ¿Has creado nuevas clases?,¿Con qué propósito?
- ¿Como has ido realizando las pruebas?,¿Has implementado algún sistema de *logging*?


## Para nota  :top:
- ¡Encuentra posibles bugs y arréglalos!
- TODO: Implementar funcionalidad de Retry
- TODO: Mostrar en el listado las diferentes combinaciones país-idioma existentes

## Algo de literatura sobre el tema :books:
- https://proandroiddev.com/mvc-mvp-mvvm-clean-viper-redux-mvi-prnsaaspfruicc-building-abstractions-for-the-sake-of-building-18459ab89386
- https://proandroiddev.com/architecture-in-jetpack-compose-mvp-mvvm-mvi-17d8170a13fd

- http://hannesdorfmann.com/android/model-view-intent/
- https://www.raywenderlich.com/817602-mvi-architecture-for-android-tutorial-getting-started
- https://medium.com/quality-content/mvi-a-reactive-architecture-pattern-45c6f5096ab7
- https://proandroiddev.com/mvi-a-new-member-of-the-mv-band-6f7f0d23bc8a
- https://quickbirdstudios.com/blog/android-mvi-kotlin-coroutines-flow/

- https://medium.com/swlh/mvi-architecture-with-android-fcde123e3c4a
- https://proandroiddev.com/mvi-architecture-with-kotlin-flows-and-channels-d36820b2028d

- https://www.youtube.com/watch?v=M34NoFI1-6I
- https://www.youtube.com/watch?v=Ls0uKLqNFz4

- https://badoo.github.io/MVICore/
- https://github.com/Tinder/StateMachine
- https://github.com/freeletics/RxRedux
- https://github.com/spotify/mobius
