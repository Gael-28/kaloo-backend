-- Kaloo - SQL Server (crear base y tablas)
-- Ejecutar en SSMS o sqlcmd contra tu instancia.
IF DB_ID('KalooDb') IS NULL CREATE DATABASE KalooDb;
GO
USE KalooDb;
GO

IF OBJECT_ID('dbo.Users', 'U') IS NULL
CREATE TABLE dbo.Users (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(100) NOT NULL UNIQUE,
    Password NVARCHAR(255) NOT NULL,
    Role NVARCHAR(50) NOT NULL -- ADMINISTRADOR | ENTRENADOR | CLIENTE
);
GO

IF OBJECT_ID('dbo.Establishments', 'U') IS NULL
CREATE TABLE dbo.Establishments (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(200) NOT NULL,
    Address NVARCHAR(500) NOT NULL,
    Phone NVARCHAR(50),
    Email NVARCHAR(200),
    OpeningHours NVARCHAR(200),
    Description NVARCHAR(MAX),
    ManagerName NVARCHAR(200),
    ManagerPhone NVARCHAR(50),
    LostAndFoundNote NVARCHAR(MAX)
);
GO

-- Migración: si la tabla ya existía, agregar las columnas nuevas
IF COL_LENGTH('dbo.Establishments', 'ManagerPhone') IS NULL
ALTER TABLE dbo.Establishments ADD ManagerPhone NVARCHAR(50);
IF COL_LENGTH('dbo.Establishments', 'ManagerName') IS NULL
ALTER TABLE dbo.Establishments ADD ManagerName NVARCHAR(200);
IF COL_LENGTH('dbo.Establishments', 'LostAndFoundNote') IS NULL
ALTER TABLE dbo.Establishments ADD LostAndFoundNote NVARCHAR(MAX);
GO

-- Rama "Racha": asistencias / racha por usuario
IF OBJECT_ID('dbo.UserStreaks', 'U') IS NULL
CREATE TABLE dbo.UserStreaks (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(100) NOT NULL UNIQUE,
    CurrentStreak INT NOT NULL DEFAULT 0,
    LongestStreak INT NOT NULL DEFAULT 0,
    TotalCheckIns INT NOT NULL DEFAULT 0,
    LastCheckIn DATE NULL
);
GO

-- Datos de ejemplo (mismos usuarios de users.json)
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = 'admin')
INSERT INTO dbo.Users (Username, Password, Role) VALUES ('admin','123','ADMINISTRADOR');
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = 'entrenador')
INSERT INTO dbo.Users (Username, Password, Role) VALUES ('entrenador','123','ENTRENADOR');
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = 'cliente')
INSERT INTO dbo.Users (Username, Password, Role) VALUES ('cliente','123','CLIENTE');
GO
