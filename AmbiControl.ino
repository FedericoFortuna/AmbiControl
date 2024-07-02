//-----------------------------------------------------
//PINES del sistema
#define PIN_SENSOR_TEMPERATURA A0
#define PIN_SENSOR_GAS A3
#define PIN_VENTILADOR 8
#define PIN_LED 3
#define PIN_TXD 7
#define PIN_RXD 11 

#include <stdio.h>
#include <string.h> 
#include <SoftwareSerial.h>   // Incluimos la librería  SoftwareSerial  
SoftwareSerial BT(7,11);    // Definimos los pines RX y TX del Arduino conectados al Bluetooth

//ESTADOS del sistema
#define PREPARADO 0
#define ILUMINANDO_Y_VENTILANDO 1
#define DESENERGIZANDO_ACTUADORES 2
#define VENTILANDO 3
#define DESENERGIZANDO_VENTILADORES 4
#define VENTILANDO_POR_BT 5

//TIMER del sistema
#define TMP_EVENTOS_MILI 2000

//EVENTOS del sistema
#define GAS_DETECTADO 50
#define GAS_NO_DETECTADO 51
#define TEMPERATURA_NORMAL 52
#define TEMPERATURA_ALTA 53
#define COMANDO_BT_ENCENDIDO 54
#define COMANDO_BT_APAGADO 55

//UMBRALES del sistema
#define UMBRAL_TEMP 25
#define UMBRAL_GAS 300

//Calculo del Voltaje
#define VOLTAJE 0.00489
#define RESTA_VOLTAJE 0.5
#define MULTIPLICADOR_VOLTAJE 100
#define VOLTAJE_TEMPERATURA 5.0

//Valores
#define MAX_ANALOG_VALUE 1023.0
#define RESISTANCE_VALUE 10000.0
#define KELVIN_A_CELCIUS 273.15

// Coeficientes de Steinhart-Hart
#define A 0.001129148
#define B 0.000234125
#define C 0.0000000876741
#define CONSTANTE_INV_STEINHART_HART 1

// Comandos BT
#define ENCENDER_VENTILADOR 'e'
#define APAGAR_VENTILADOR 'a'

//-----------------------------------------------------

//-----------------------------------------------------
// Estructuras de datos
struct structSensor
{
  int pin;
  int nivel;
};

struct structActuador 
{
  int pin;
};


struct structEvento
{
  int tipo;
  int valor;
};

// Variables Globales del Sistema
float temp;
structSensor sensor_gas;
structSensor sensor_temperatura;
structActuador actuador_led;
structActuador actuador_ventilador;
int estado_actual;
unsigned long tiempo_anterior;
unsigned long tiempo_actual;
structEvento evento;
//-----------------------------------------------------

//-----------------------------------------------------
// Main del Sistema
void setup()
{
  BT.begin(9600);       // Inicializamos el puerto serie BT (Para Modo AT 2)
  Serial.begin(9600);

  sensor_gas.pin = PIN_SENSOR_GAS;
  pinMode(sensor_gas.pin, INPUT);

  sensor_temperatura.pin = PIN_SENSOR_TEMPERATURA;
  pinMode(sensor_temperatura.pin, INPUT); 
  
  actuador_led.pin = PIN_LED;
  pinMode(actuador_led.pin, OUTPUT);

  actuador_ventilador.pin = PIN_VENTILADOR;
  pinMode(actuador_ventilador.pin, OUTPUT);

  estado_actual = PREPARADO;

  tiempo_anterior = millis();
}

void loop()
{
  procesar();
}

void procesar()
{
  tiempo_actual = millis(); // Actualizamos el tiempo actual en cada iteración

  // Llamamos a tomar_evento() si ha pasado el tiempo de intervalo y hay eventos relevantes
  if ((tiempo_actual - tiempo_anterior) > TMP_EVENTOS_MILI)
  {
    tomar_evento();
    tiempo_anterior = tiempo_actual; // Actualizamos el tiempo anterior solo cuando tomamos un evento
    switch (estado_actual)
    {
      case PREPARADO:
      {
          switch (evento.tipo)
          {
              case GAS_DETECTADO:
              {
                    gasDetectado(PREPARADO);
              }
              break;
            
              case TEMPERATURA_ALTA:
              {
                  temperaturaAlta(PREPARADO);
              }
              break;

              case COMANDO_BT_ENCENDIDO:
              {
                    encenderVentiladorBT(PREPARADO);
              }
              break;
          }
          break;
      }
      break;

      case ILUMINANDO_Y_VENTILANDO:
      {
          switch (evento.tipo)
          {
            case GAS_NO_DETECTADO:
            {
                gasNoDetectado(ILUMINANDO_Y_VENTILANDO);
            }
            break;
          }
      break;
      }
      break;

      case DESENERGIZANDO_ACTUADORES:
      {
          switch (evento.tipo)
          {
            case TEMPERATURA_ALTA:
            {
                  temperaturaAlta(DESENERGIZANDO_ACTUADORES);
            }
            break;

            case GAS_DETECTADO:
            {
                gasDetectado(DESENERGIZANDO_ACTUADORES);
            }
            break;

            case COMANDO_BT_ENCENDIDO:
            {
                  encenderVentiladorBT(DESENERGIZANDO_ACTUADORES);
            }
            break;
          }
          break;
      }
      break;

      case VENTILANDO:
      {
          switch (evento.tipo)
          {
            case TEMPERATURA_NORMAL:
            {
                  temperaturaNormal(VENTILANDO);
            }
            break;

            case GAS_DETECTADO:
            {
                gasDetectado(VENTILANDO);
            }
            break;

            case COMANDO_BT_ENCENDIDO:
            {
                  encenderVentiladorBT(VENTILANDO);
            }
            break;
          }
          break;
      }
      break;

      case DESENERGIZANDO_VENTILADORES:
      {
          switch (evento.tipo)
          {
            case TEMPERATURA_ALTA:
            {
                  temperaturaAlta(DESENERGIZANDO_VENTILADORES);
            }
            break;

            case GAS_DETECTADO:
            {
                gasDetectado(DESENERGIZANDO_VENTILADORES);
            }
            break;

            case COMANDO_BT_ENCENDIDO:
            {
                encenderVentiladorBT(DESENERGIZANDO_VENTILADORES);
            }
            break;
          }
          break;
      }
      break;

      case VENTILANDO_POR_BT:
      {
          switch (evento.tipo)
          {
            case GAS_DETECTADO:
            {
                  gasDetectado(VENTILANDO_POR_BT);
            }
            break;

            case COMANDO_BT_APAGADO:
            {
                apagarVentiladorBT(VENTILANDO_POR_BT);
            }
            break;
          }
          break;
      }
      break;
    }

  }
    
}
//-----------------------------------------------------

//-----------------------------------------------------
// Funciones del Sistema
void tomar_evento()
{

    if(verificar_sensor_gas() || verificar_sensor_temperatura() || verificar_bt() )
       return;    
}

bool verificar_sensor_temperatura()
{
  sensor_temperatura.nivel = leer_sensor_temperatura(sensor_temperatura.pin);  
  Serial.print("TEMPERATURA SENSADA: "); Serial.println(sensor_temperatura.nivel);  
  Serial.println("---------------------------------------------");
  
  if(sensor_temperatura.nivel > UMBRAL_TEMP )
  {
      evento.tipo = TEMPERATURA_ALTA;
      return true;
  }
  else
  {
      evento.tipo = TEMPERATURA_NORMAL;
   	  return false;
  }
}

bool verificar_sensor_gas()
{
  sensor_gas.nivel = leer_sensor_gas(sensor_gas.pin);
  Serial.print("GAS SENSADO: "); Serial.println(sensor_gas.nivel);  
  Serial.println("---------------------------------------------");
  
  if(sensor_gas.nivel > UMBRAL_GAS )
  {
      evento.tipo = GAS_DETECTADO;
      return true;
  }
  else
  {
	  evento.tipo = GAS_NO_DETECTADO;
	  return false;    
  }

}

bool verificar_bt()
{
  if(BT.available())    // Si llega un dato por el puerto BT se envía al monitor serial
  {
    char caracterLeido = BT.read();
    if( caracterLeido == ENCENDER_VENTILADOR){ //si llega un e prendo el ventilador
        Serial.println("PRENDIENDO MOTORES");
        Serial.println("---------------------------------------------");
        evento.tipo = COMANDO_BT_ENCENDIDO;
    }else{
      if( caracterLeido == APAGAR_VENTILADOR){ //si llega una a apago el ventilador
        Serial.println("APAGANDO MOTORES");
        Serial.println("---------------------------------------------");
        evento.tipo = COMANDO_BT_APAGADO;
      }
    }
    return true;  
  }else
  {
    return false;
  }
}

int leer_sensor_temperatura(int pin)
{
  
	int sensor_value = analogRead(pin);
  	float voltage = sensor_value * (VOLTAJE_TEMPERATURA / MAX_ANALOG_VALUE);
  
  	// Convertir el voltaje a resistencia del termistor
  	float resistance = (VOLTAJE_TEMPERATURA - voltage) * RESISTANCE_VALUE / voltage;


  	// Calcular la temperatura usando la fórmula del termistor (simplificada) de Steinhart-Hart
  	float temperatureC = CONSTANTE_INV_STEINHART_HART / (A + (B * log(resistance)) + (C * pow(log(resistance), 3))) - KELVIN_A_CELCIUS;

    char temperaturaActual[20];
    int tempActual = temperatureC;
    sprintf(temperaturaActual,"%d",tempActual);
    char mensajeAndroid[100];
    snprintf(mensajeAndroid, sizeof(mensajeAndroid), "%d|%s", estado_actual, temperaturaActual);
    BT.write(mensajeAndroid);

  return temperatureC;
  
}

int leer_sensor_gas(int pin)
{
	return analogRead(pin); 
}

void generarLogs(int estado_actual, int evento_actual, char* mensaje)
{
  	Serial.print("ACCION: "); Serial.println(mensaje);
	Serial.print("ESTADO: "); Serial.println(estado_actual);
	Serial.print("EVENTO: "); Serial.println(evento_actual);
	Serial.println("---------------------------------------------");
}

void actualizarEstado(int estado_nuevo)
{
	estado_actual = estado_nuevo;
}

void encenderLed()
{
	digitalWrite(PIN_LED, HIGH);  
}

void encenderVentilador()
{
	digitalWrite(PIN_VENTILADOR, HIGH);	  
}

void apagarLed()
{
	digitalWrite(PIN_LED, LOW);  
}

void apagarVentilador()
{
	digitalWrite(PIN_VENTILADOR, LOW);	  
}

void gasDetectado(int estado_actual)
{
  actualizarEstado(ILUMINANDO_Y_VENTILANDO);
  encenderLed();
  encenderVentilador();
  char mensaje[] = "Encendiendo Ventiladores y Led";
  generarLogs(estado_actual, GAS_DETECTADO, mensaje);  
  mandarMensajeAndroid();
}

void temperaturaAlta(int estado_actual)
{
  actualizarEstado(VENTILANDO);
  encenderVentilador();
  char mensaje[] = "Encendiendo Ventiladores";
  generarLogs(estado_actual, TEMPERATURA_ALTA, mensaje);
  mandarMensajeAndroid();
}

void encenderVentiladorBT(int estado_actual)
{
  actualizarEstado(VENTILANDO_POR_BT);
  encenderVentilador();
  char mensaje[] = "Encendiendo Ventiladores";
  generarLogs(estado_actual, COMANDO_BT_ENCENDIDO, mensaje);
  mandarMensajeAndroid();
}

void apagarVentiladorBT(int estado_actual)
{
  actualizarEstado(PREPARADO);
  apagarVentilador();
  char mensaje[] = "Apagando Ventiladores";
  generarLogs(estado_actual, COMANDO_BT_APAGADO, mensaje);
  mandarMensajeAndroid();
}

void gasNoDetectado(int estado_actual)
{
  actualizarEstado(DESENERGIZANDO_ACTUADORES);
  apagarLed();
  apagarVentilador();
  char mensaje[] = "Apagando Actuadores";
  generarLogs(estado_actual, GAS_NO_DETECTADO, mensaje);
  mandarMensajeAndroid();
}

void temperaturaNormal(int estado_actual)
{
  actualizarEstado(DESENERGIZANDO_VENTILADORES);
  apagarVentilador();
  char mensaje[] = "Apagando Ventiladores";
  generarLogs(estado_actual, TEMPERATURA_NORMAL, mensaje);
  mandarMensajeAndroid();
}

void mandarMensajeAndroid(){
  char temperaturaActual[20];
  int tempActual = leer_sensor_temperatura(sensor_temperatura.pin);
  sprintf(temperaturaActual,"%d",tempActual);
  char mensajeAndroid[100];
  snprintf(mensajeAndroid, sizeof(mensajeAndroid), "%d|%s", estado_actual, temperaturaActual);
  BT.write(mensajeAndroid);
}
//-----------------------------------------------------