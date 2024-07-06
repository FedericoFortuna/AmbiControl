package com.example.bluetooth.utils;

public class Constants 
{
    public final static String TEXT_PREPARADO = "Preparado para comenzar";
    public final static String TEXT_ILUMINANDO_Y_VENTILANDO = "Led y ventiladores encendidos";
    public final static String TEXT_DESENERGIZANDO_ACTUADORES = "Apagando led y ventiladores";
    public final static String TEXT_VENTILANDO = "Ventiladores encendidos";
    public final static String TEXT_DESENERGIZANDO_VENTILADORES = "Apagando ventiladores";
    public final static String TEXT_VENTILANDO_POR_BT = "Ventiladores encendidos mediante comando bluetooth";

    public final static Integer CODE_PREPARADO = 0;
    public final static Integer CODE_ILUMINANDO_Y_VENTILANDO = 1;
    public final static Integer CODE_DESENERGIZANDO_ACTUADORES = 2;
    public final static Integer CODE_VENTILANDO = 3;
    public final static Integer CODE_DESENERGIZANDO_VENTILADORES = 4;
    public final static Integer CODE_VENTILANDO_POR_BT = 5;

    public final static String TEXT_TEST_CONNECTION = "x";
    public final static String APAGAR_VENTILADOR_BT = "a";
    public final static String ENCENDER_VENTILADOR_BT = "e";

    public final static String ACTIVATED_BT = "Bluetooth Habilitado";
    public final static String DEACTIVATED_BT = "Bluetooth Deshabilitado";

    public final static String DEACTIVATE = "Desactivar";
    public final static String ACTIVATE = "Activar";
    public final static String BT_NOT_SUPPORTED = "Bluetooth no es soportado por el dispositivo movil";
    public final static String FOUNDED_DEVICE = "Dispositivo Encontrado:";
    public final static String NOT_PAIRED_DEVICE_FOUNDED = "No se encontraron dispositivos emparejados";

    public final static Integer REQUEST_CODE = 1000;
    public final static String PAIRING = "Emparejando";
    public final static String BT_ADDRESS = "Direccion_Bluethoot";
    public final static String NOT_PAIRED = "No emparejado";

    public final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public final static Integer THRESHOLD = 50;

    public final static String CHANNEL_ID = "sensor_notification_channel";

    public final static String SOCKET_CREATION_FAILED = "La creacción del Socket fallo";

    public final static String NOTIFICATION_NAME =  "Sensor Notification";
    public final static String NOTIFICATION_DESC = "Gas alto detectado";
    public final static String FAILED_CONNECTION = "La conexion fallo";

    public final static String HIGH_GAS_ALERT_NOTIFICATION = "Alerta de Gas Alto";
    public final static String HIGH_GAS_DETECTED_NOTIFICATION = "Gas alto detectado";

    public final static String FAN_TURN_ON_VIA_SHAKE = "Encendiendo ventilador por shake";
    public final static String FAN_TURN_OFF_VIA_SHAKE = "Apagando ventilador por shake";

}
