package org.firstinspires.ftc.teamcode.sensors;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class Gyro
{
    private IMU imu;
    public void init(@NonNull HardwareMap hardwareMap)
    {
        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        );
        imu.initialize(new IMU.Parameters(orientation));
    }
    public double getHeading(AngleUnit angleUnit)
    {
        return imu.getRobotYawPitchRollAngles().getYaw(angleUnit);
    }
    public void ResetYaw()
    {
        imu.resetYaw();
    }
}
