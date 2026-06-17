-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1:3306
-- Tiempo de generación: 16-06-2026 a las 23:02:59
-- Versión del servidor: 8.4.7
-- Versión de PHP: 8.3.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `juego`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `partidas`
--

DROP TABLE IF EXISTS `partidas`;
CREATE TABLE IF NOT EXISTS `partidas` (
  `id_partida` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `puntaje` int NOT NULL DEFAULT '0',
  `fecha_partida` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_partida`),
  KEY `fk_partida_usuario` (`id_usuario`)
) ENGINE=MyISAM AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `partidas`
--

INSERT INTO `partidas` (`id_partida`, `id_usuario`, `puntaje`, `fecha_partida`) VALUES
(1, 1, 41, '2026-06-08 22:22:18'),
(2, 3, 230, '2026-06-09 03:18:32'),
(3, 4, 235, '2026-06-15 02:14:45'),
(4, 5, 1119, '2026-06-15 02:18:46'),
(9, 6, 5679, '2026-06-15 03:47:53'),
(8, 6, 7536, '2026-06-15 03:33:54');

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `ranking`
-- (Véase abajo para la vista actual)
--
DROP VIEW IF EXISTS `ranking`;
CREATE TABLE IF NOT EXISTS `ranking` (
`mejor_puntaje` int
,`nombre_usuario` varchar(50)
);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `nombre_usuario` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contraseña` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario` (`nombre_usuario`),
  UNIQUE KEY `correo` (`correo`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre_usuario`, `correo`, `contraseña`, `fecha_registro`) VALUES
(6, 'esteban', NULL, '8761bc707d28b114733653f5255e8d13ab8868925176ad2c0942231fa311e30a', '2026-06-15 02:44:37');

-- --------------------------------------------------------

--
-- Estructura para la vista `ranking`
--
DROP TABLE IF EXISTS `ranking`;

DROP VIEW IF EXISTS `ranking`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `ranking`  AS SELECT `u`.`nombre_usuario` AS `nombre_usuario`, min(`p`.`puntaje`) AS `mejor_puntaje` FROM (`usuarios` `u` join `partidas` `p` on((`u`.`id_usuario` = `p`.`id_usuario`))) GROUP BY `u`.`id_usuario`, `u`.`nombre_usuario` ORDER BY `mejor_puntaje` ASC ;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
