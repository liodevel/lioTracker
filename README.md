# My Tracker

Versión actual en desarrollo
1.7

OK - Botón seleccionar medio de transporte en MapActivity, Spinner de imágenes?

OK . Fabric y crashlytics

OK - MapView con MapBox (OpenStreetMaps)

 - TrackView con MapBox (OpenStreetMaps)
    Presentación guapa 

- App ERROR al cerrar la app desde trackView

- Más social, compartir mapa, imagen, facebook

- Refrescar bien la lista de rutas, cuando se repara una, no se actualiza hasta que se vuelve a cargar la lista.



1.6

OK - PROBAR - Guardar tracks OffLine (Local datastore)?
- Inicio Offline si no se puede guardar el Track, a partir de ahí, se ha de guardar un track, y todos los trackpoints en un array...
Un Array de Tracks, y guardarlos en el sharedpreferences, y comprobar cuando se arranca la app si hay conexión, y enviar los tracks a Parse.

OK - Vigilar si se entra en una lista cuando se está registrando, puede que estropee el track actual al intentar repararlo

OK - Linkar Instalación con usuario

OK - Simplificar confifuración, sólo una pantalla

OK - Coger datos de los login por facebook

- Gráfica de colores dinámica en MapActivity, los cuadros de colores se irán agrandando dependiendo de la velocidad registrada.







# Siguentes versiones
# 2.0

- de momento no- Led cuando está registrando (Valorar)

- "Password olvidado" Botón en pantalla de login, cambio de password, envío de email. En caso de no ser login por facebook.

- Añadir tags a las rutas, para filtrarlas en la lista

- Añadir puntos en ruta, Foto, grabación de voz, o texto...

- Añadir int.proveedor en track

- Para tracks muy largos, resumir la cantidad de trackpoints a mostrar. 
  Query por tiempo, o trackpoints a intervalos. No he encontrado la solución.


# Más adelante

- Añadir en opciones, Exportar KML con velocidades o en negro, avisando de que en colores ocupa mucho más.


# Temas cerrados

- El tiempo de las notificaciones no es el mismo que el del cronómetro
- Al volver de opciones, pasa por splash Screen
- Notificaciones
  - Cuando esta registrando un track, mostrarlo en notificaciones.
  - Mostrar el tiempo y distancia registrada en las notificaciones. 
  - Al hacer click, que vaya a  MapActivity.
  - Al parar de registrar, cerrar la notificación.
  - No se debe poder cerrar.
- API 16 no parece funcionar... 19 OK - MIN SDK cambiado a 19
- Permitir el inicio de la App Offline, guardando el ParseUser en SharedPreferences. (No hizo falta, Parse ya lo gestiona) :)
- Mostrar tiempo del track mientras se registra. Implementar un timer
- Banda blanca parte arriba en MapView (Móviles pequeños)
- Controlar pulsar botón de startrack cuando esta localizando

1.3 -Colores velocidades mejorados, Exportar a GoogleMaps y Earth, frecuencia de registro en opciones,

- Mejorar Splash Screen
- Mostrar Velocidad media en Mis Rutas y en la TrackView (Añadir color?)
- Implementar "Mis Tracks favoritos"
- Añadir leyenda de velocidades
- Añadir más colores a las velocidades: 
    0-10 negro
    10-20 rojo
    20-30 NARANJA,
    30-40 amarillo
    40-50 verde
    50-70 VERDE OSCURO, 
    70-90 azul, 
    90-110 cyan, 
    > 110 magenta
- 1.1 - Dias de la semana en castellano
- Comprobar usuarios duplicados al registrarse
- Preguntar al parar de registrar un track
- Añadir en opciones la frecuencia de envio de trackpoints, defecto, y valor mínimo 5, máximo 60
- Si está registrando, no dejar salir?
- La velocidad no se muestra correctamente en MapView
- (PROBAR) Arreglar problema de la falta de cobertura GPS, envios de trackpoints con la misma LatLong
- Imágenes promocionales y texto para google play
- Nombre del usuario en nav drawer
- Valorar implementar drawer para ir a otros apartados
- Reparar tracks mal cerrados al recuperar la lista 
- Poner flechas Atrás en MyTracks y TrackView 
- Drawer en Map view y eliminar 3 puntos de la derecha
- Mostrar velocidad en tramos mientras se graba el Track
- Poner horas en formato correcto 
- Al borrar, no se reinicia algun array o variable
- Color de la ruta en función de la velocidad. Investigar que colores utilizar
- La fecha de fin del Track debe ser la del último trackpoint
- Multi idioma, añadir castellano
- Login with Facebook https://developers.facebook.com/quickstarts/1664125210531403/?platform=android
- Medidas de iconos de la ActionBar a 24dp
- TrackView se queda siempre activa
- Implementar Tracks favoritas
- En MapView y trackView poder poner el mapa en vista satelite
- Comprobar GPS encendido, y avisar
- Poder editar la info del Track
- Añadir Info al Track
- Cambiar marker del mapa por el hexágono verde del logo
- Implementar Settings: Sólo GPS
- Lista Tracks multiselect
- Cambiar icono de Centrar mapa
- Icono animado al cargar Track
- Icono animado al cargar lista de Tracks
- Mostrar datos de Track en TrackActivity
- Calcular distancia recorrida
- Cambiar icono de startTracking, boton REC, y que cambie a roja cuando está registrando
- Dibujar Logo
- Borrar tracks desde la lista
- Implementar pantalla Splash, donde se comprobará el inicio de sesión
- Botón cerrar sesión
- Añadir más iconos a la aplicación
- Botón eliminar Track en TrackActivity
- Lista tracks salen duplicados
- Click en un track, abrir activity com un mapa con la ruta dibujada, y los datos
- Lista My Tracks
- Implementar ActioBar para alojar todas las acciones
- Boton centrar vista
- Dibujar trazado mientras "tracking"
- Ampliar clase "track", fecha finalización, distancia recorrida, velocidad media, medio de transporte.
