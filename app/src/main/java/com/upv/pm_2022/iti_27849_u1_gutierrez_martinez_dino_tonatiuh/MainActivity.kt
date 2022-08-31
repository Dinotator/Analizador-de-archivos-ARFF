package com.upv.pm_2022.iti_27849_u1_gutierrez_martinez_dino_tonatiuh

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_ID_READ_PERMISSION = 100
    private val REQUEST_ID_WRITE_PERMISSION = 200 //Estos son los permisos
    var CX: Context? = null

    //Declaramos las variables en donde se van a almacenar los objetos
    lateinit var mostrarArchivosM: TextView
    lateinit var mostrarColumnasM: TextView
    lateinit var mostrarCamposM: TextView
    lateinit var mostrarTipoCM: TextView
    lateinit var mostrarPromedioM: TextView
    lateinit var mostrarMaximoM: TextView
    lateinit var mostrarMinimoM: TextView
    lateinit var mostrarDEM: TextView
    lateinit var mostrarValoresNominalesM: TextView
    lateinit var ingresarNombreAM: EditText

    //Declaramos los arreglos de los cuales manipularemos los datos
    val datosFiltrados = ArrayList<ArrayList<Any>>()
    var columnasIdentificadas = ArrayList<String>()
    var columnasAsignadas = ArrayList<String>()

    //Es la ruta en la que se va a encontrar la carpeta
    var ruta = Environment.getExternalStorageDirectory().absolutePath.toString() + "/" + "arffFiles"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mostrarArchivosM = findViewById(R.id.mostrarArchivos)
        mostrarColumnasM = findViewById(R.id.mostrarColumnas)
        mostrarCamposM = findViewById(R.id.mostrarCampos)
        mostrarTipoCM = findViewById(R.id.mostrarTipoC)
        mostrarPromedioM = findViewById(R.id.mostrarPromedio)
        mostrarMaximoM = findViewById(R.id.mostrarMaximo)
        mostrarMinimoM = findViewById(R.id.mostrarMinimo)
        mostrarDEM = findViewById(R.id.mostrarDE)
        mostrarValoresNominalesM = findViewById(R.id.mostrarValoresNominales)
        ingresarNombreAM = findViewById(R.id.ingresarNombreA)

        val btnListaArchivos = findViewById<Button>(R.id.listaArchivos)
        val btnAnalizarArchivo = findViewById<Button>(R.id.analizarArchivo)

        //Se declaran ambas variables para detectar los atributos del archivo ARFF
        val atributoMay = "@ATTRIBUTE"
        val atributoMin = "@attribute"

        //Se solicita los permisos
        askPermissionOnly();
        CX = this;

        //Agregamos un OnClick para mostrar los archivos
        btnListaArchivos.setOnClickListener{
            verificacionCarpeta() // Método para verificar la carpeta y mostrar los archivos
        }

        //Este OnClick es el que se encarga de llamar los métodos para analizar, manipular y mostrar los datos solicitados
        btnAnalizarArchivo.setOnClickListener{
            try {
                lecturaArchivo()
                analizarColumnas(atributoMay, atributoMin)
                analizarCampos(atributoMay, atributoMin)
                analizarTipoColumna(atributoMay,atributoMin)
                asignacionEstadisticas()

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    //Analiza la existencia de la carpeta y si ésta contiene archivo .arff
    private fun verificacionCarpeta() {

        val carpeta = File(ruta) //Almacena la ruta en donde se encuentra la carpeta

        // Sentencia para verificar la existencia de la carpeta
        if (!carpeta.exists()) {
            carpeta.mkdir() //Si no existe la carpeta, se crea una con el nombre que se indica
            Toast.makeText(this, "No se encontró ninguna carpeta... Creando una nueva", Toast.LENGTH_SHORT).show()
            return
        } else {
            Toast.makeText(this, "Carpeta leída exitosamente", Toast.LENGTH_SHORT).show()
            // Se guardan los archivos
            var nombreArchivos = ""
            val archivos = carpeta.listFiles()
            val nombres = arrayOfNulls<String>(archivos.size)

            //Se lee la carpeta y dependiendo de cuantos archivos existan, es la cantidad de veces en las que se itera el for
            for (i in nombres.indices) {
                val name = archivos[i].name.replace(".", ";")
                nombres[i] = name.split(";").toTypedArray()[0]
                if (nombres.get(i) == "") {
                } else {
                    nombreArchivos += nombres.get(i).toString() + "\n"
                }
            }

            //Al analizar la carpeta, muestra los archivos .arff que se encuentran disponibles
            mostrarArchivosM.setText(nombreArchivos)
        }

    }

    private fun lecturaArchivo(){
        val carpeta = File(ruta)
        if (carpeta.exists()) { // Ya existe el directorio
            val F2 = File(ruta,ingresarNombreAM.text.toString()+ ".arff")

            if (F2.exists()) { // Checar si el archivo Existe
                //Se declaran las variables para almacenar la ruta y el lector que tendrá como parámetro la ruta dada y otra variable
                //para ir guardando las líneas
                val fIn = FileInputStream(F2)
                val myReader = BufferedReader(InputStreamReader(fIn))
                var aDataRow = ""

                val reader = BufferedReader(myReader)
                var line : String? = ""

                //Ciclo para leer línea por línea el archivo
                while (line != null) {
                    line = reader.readLine()
                    aDataRow += line + "\n"
                }

                myReader.close()

                Toast.makeText(this, "Archivo Analizado Correctamente", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(
                    applicationContext,
                    "Ingrese archivos .arff, este archivo no es valido en el Directorio: " + ruta, Toast.LENGTH_SHORT).show()
            }
        } else { // No existe, crea el directorio
            Toast.makeText(
                applicationContext,
                "Se ha creado la ruta RUTA:$ruta",
                Toast.LENGTH_SHORT
            ).show()
            carpeta.mkdir()
        }


    }
//Estudiante de la UAT de la tarde el que lo lea
    @Throws(IOException::class)
    fun analizarColumnas(may: String?, min: String?) {
        var total = 0
        val carpeta = File(ruta)
        if (carpeta.exists()) { // Valida que exista la carpeta
            //Declaramos esta variable para indicar que se va a guardar la ruta y nombre del archivo
            val F2 = File(ruta, ingresarNombreAM.text.toString() + ".arff")

            if (F2.exists()) { // Checa si el archivo Existe
                // Checar si el archivo Existe
                val fIn = FileInputStream(F2)
                val myReader = BufferedReader(InputStreamReader(fIn))

                //Ciclo que se encarga de leer línea por línea el archivo
                while (true) {
                    //Esta variable va almacenando la lectura del archivo
                    val line = myReader.readLine() ?: break
                    //Almacena las líneas del archivo y los separa según los espacios
                    val partes = line.split(" ").toTypedArray()
                    //Este ciclo consiste en comparar todas las palabras separadas por espacio
                    //Y cada vez que pasa por @ATTRIBUTE o @attribute se agrega al contador para indicar cuantas columnas hay
                    for (i in partes.indices) {
                        if (partes[i] == may) {
                            total = total + 1
                        } else {
                            if (partes[i] == min) {
                                total = total + 1
                            }
                        }
                    }
                }
                myReader.close()

                //Al final se guarda en otra variable el total de columnas que hay y los muestra
                var mostrar = total.toString()
                mostrarColumnasM.setText("Número de columnas: " + mostrar)

            }

        }
    }

    //Método que analiza que tipo de campos hay en el archivo
    @Throws(IOException::class)
    fun analizarCampos(may: String?, min: String?) {
        var total = 0
        val carpeta = File(ruta)
        if (carpeta.exists()) { // Ya existe el directorio
            val F2 = File(ruta, ingresarNombreAM.text.toString() + ".arff")

            if (F2.exists()) { // Checa si el archivo Existe

                val fIn = FileInputStream(F2)
                val myReader = BufferedReader(InputStreamReader(fIn))

                var contenido = ""
                var verificarDatos = false


                //Este ciclo es parecido al anterior, sin embargo, filtra las cadenas que sean datos
                while (true) {
                    val line = myReader.readLine() ?: break
                    val partido = line.split(" ").toTypedArray()
                    for (i in partido.indices) {
                        if (verificarDatos) {
                            val filas = partido[0].split(",").toTypedArray()
                            if (datosFiltrados.isEmpty()) {
                                for (j in filas.indices) {
                                    val temporal = ArrayList<Any>()
                                    temporal.add(filas[j])
                                    datosFiltrados.add(temporal)
                                }
                            } else {
                                for (j in filas.indices) {
                                    datosFiltrados.get(j).add(filas[j])
                                }
                            }
                        }
                        if (partido[i] == "@DATA") {
                            verificarDatos = true
                        }
                        if (partido[i] == may) {
                            contenido += """
            ${partido[1]}
            
            """.trimIndent()

                        } else {
                            if (partido[i] == min) {
                                contenido += """
                ${partido[1]}
                
                """.trimIndent()
                            }
                        }
                    }


                }

                myReader.close()


                columnasAsignadas = ArrayList(Arrays.asList(*contenido.split("\n").toTypedArray()))
                mostrarCamposM.setText(contenido)

            }

        }
    }


    @Throws(IOException::class)
    fun analizarTipoColumna(may: String?, min: String?) {
        var total = 0
        val carpeta = File(ruta)
        if (carpeta.exists()) { // Ya existe el directorio
            val F2 = File(ruta, ingresarNombreAM.text.toString() + ".arff")

            if (F2.exists()) { // Checar si el archivo Existe
                // Checar si el archivo Existe
                val fIn = FileInputStream(F2)
                val myReader = BufferedReader(InputStreamReader(fIn))

                var tipoColumna = ""

                while (true) {
                    val line = myReader.readLine() ?: break

                    val partido = line.split(" ").toTypedArray()
                    for (i in partido.indices) {
                        if (partido.get(i) == may) {
                            tipoColumna += partido.get(2).toString() + "\n"
                        } else {
                            if (partido.get(i) == min) {
                                tipoColumna += partido.get(2).toString() + "\n"
                            }
                        }
                    }

                }
                columnasIdentificadas = ArrayList(Arrays.asList(*tipoColumna.split("\n").toTypedArray()))

                mostrarTipoCM.setText(tipoColumna)


            }

        }
    }


    @Throws(IOException::class)
    fun asignacionEstadisticas(){

        var salidaMinimo = ""
        var salidaMaximo = ""
        var salidaPromedio = ""
        var salidaDesviacion = ""
        var salidaNominal = ""
        for (i in columnasIdentificadas.indices) {
            if (columnasIdentificadas[i] == "REAL" || columnasIdentificadas[i] == "NUMERIC" || columnasIdentificadas[i] == "numeric") {
                salidaPromedio+= columnasAsignadas.get(i) + " " + columnasIdentificadas.get(i) +""" Promedio = ${String.format("%.2f", promedio(datosFiltrados[i]))} """ + "\n"
                salidaMaximo+= columnasAsignadas.get(i) + " " + columnasIdentificadas.get(i) +""" Máximo = ${String.format("%.2f", valorMaximo(datosFiltrados[i]))}""" + "\n"
                salidaMinimo+= columnasAsignadas.get(i) + " " + columnasIdentificadas.get(i) +""" Mínimo = ${String.format("%.2f", valorMinimo(datosFiltrados[i]))}""" + "\n"
                salidaDesviacion+= columnasAsignadas.get(i) + " " + columnasIdentificadas.get(i) +""" DE = ${String.format("%.2f", desviacionEstandar(datosFiltrados[i]))}""" + "\n"

            }else{
                var salida = ""
                var columnas = columnasIdentificadas[i].replace("{", "")
                columnas = columnas.replace("}", "")
                val clases = columnas.split(",").toTypedArray()
                for (j in clases.indices) {
                    var contador = 0
                    for (valor in datosFiltrados[j]) {
                        if (valor.toString() == clases[j]) {
                            contador++
                        }
                        salida += columnasAsignadas.get(i) + " " + columnasIdentificadas.get(i) + "columnas = " +
                                """"${clases[j]} ${contador}""" + "\n"
                    }

                }
                salidaNominal+= salida
                mostrarValoresNominalesM.setText(salidaNominal)

            }

        }
        mostrarPromedioM.setText(salidaPromedio)
        mostrarMaximoM.setText(salidaMaximo)
        mostrarMinimoM.setText(salidaMinimo)
        mostrarDEM.setText(salidaDesviacion)

    }

    //A partir de aquí solo son funciones para calcular las estadísticas básicas
    private fun valorMaximo(lista: ArrayList<Any>): Double {
        var valorMax = Double.MIN_VALUE
        for (valor in lista) {
            try {
                if (valor.toString().toDouble() > valorMax) {
                    valorMax = valor.toString().toDouble()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return valorMax
    }

    private fun valorMinimo(lista: ArrayList<Any>): Double {
        var valorMin = Double.MAX_VALUE
        for (valor in lista) {
            try {
                if (valor.toString().toDouble() < valorMin) {
                    valorMin = valor.toString().toDouble()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return valorMin
    }


    private fun desviacionEstandar(lista: ArrayList<Any>): Double {
        return Math.sqrt(varianza(lista))
    }

    private fun varianza(lista: ArrayList<Any>): Double {
        val prom: Double = promedio(lista)
        val dif: Double = diferencia(lista, prom)
        return dif / lista.size
    }

    private fun promedio(lista: ArrayList<Any>): Double {
        var prom = 0.0
        for (valor in lista) {
            try {
                prom += valor.toString().toDouble()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return prom / lista.size
    }

    private fun diferencia(lista: ArrayList<Any>, prom: Double): Double {
        var suma = 0.0
        var i = lista.size
        while (i > -1) {
            try {
                suma += Math.pow(lista[i].toString().toDouble() - prom, 2.0)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            i--
        }
        return suma
    }

    /*
     *   Funciones especializadas en la obtanción de Permisos de USUARIO !!!!!
     *   Sacadas de algún lado de StackOverFlow...
     *
     * */
    private fun askPermissionOnly() {
        askPermission(
            REQUEST_ID_WRITE_PERMISSION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        askPermission(
            REQUEST_ID_READ_PERMISSION,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    // With Android Level >= 23, you have to ask the user
    // for permission with device (For example read/write data on the device).
    private fun askPermission(requestId: Int, permissionName: String): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {

            // Check if we have permission
            val permission = ActivityCompat.checkSelfPermission(this, permissionName)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                requestPermissions(
                    arrayOf(permissionName),
                    requestId
                )
                return false
            }
        }
        return true
    }

    // When you have the request results
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        // Note: If request is cancelled, the result arrays are empty.
        if (grantResults.size > 0) {
            when (requestCode) {
                REQUEST_ID_READ_PERMISSION -> {
                    run {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(applicationContext,"Permission Lectura Concedido!",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    run {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            //writeFile();
                            //
                            Toast.makeText(applicationContext,"Permission Escritura Concedido!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                REQUEST_ID_WRITE_PERMISSION -> {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(
                            applicationContext,
                            "Permission Escritura Concedido!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(applicationContext, "Permission Cancelled!", Toast.LENGTH_SHORT).show()
        }

        // check condition
        if (requestCode == 1 && grantResults.size > 0 && (grantResults[0]
                    == PackageManager.PERMISSION_GRANTED)
        ) {
            Toast.makeText(applicationContext,"Permission Escritura Concedido!", Toast.LENGTH_SHORT).show()
        } else {
            // When permission is denied
            // Display toast
            Toast.makeText(applicationContext,"Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}