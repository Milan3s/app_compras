-- --------------------------------------------------------
-- SCRIPT DE DATOS BASE (ordenado correctamente)
-- --------------------------------------------------------

-- 1️⃣ ROLES (primero, porque los usuarios dependen de ellos)
INSERT INTO `roles` (`id_rol`, `nombre`, `descripcion_rol`) VALUES
(1, 'Administrador', 'Acceso completo al sistema.'),
(2, 'Padre', 'Puede gestionar compras y revisar historial.'),
(3, 'Madre', 'Puede gestionar compras y agregar productos.'),
(4, 'Hijo', 'Puede registrar compras propias.'),
(5, 'Hija', 'Puede registrar compras propias.'),
(6, 'Pendiente', 'Por aprobar por el administrador.');

-- 2️⃣ PERMISOS (luego, porque roles_permisos y usuarios_permisos dependen de ellos)
INSERT INTO `permisos` (`id_permiso`, `nombre`, `descripcion`, `area`) VALUES
(1, 'btnHacerCompra', 'Permite realizar una compra.', 'Hacer Compras'),
(2, 'btnListadoDeCompras', 'Permite ver el listado de compras.', 'Listado de Compras'),
(3, 'btnGestionTiendas', 'Permite gestionar tiendas.', 'Gestión de Tiendas'),
(4, 'btnGestionProductos', 'Permite gestionar productos.', 'Gestión de Productos'),
(5, 'btnGestionCategorias', 'Permite gestionar categorías.', 'Gestión de Categorías'),
(6, 'btnGestionUsuarios', 'Permite gestionar usuarios.', 'Gestión de Usuarios'),
(7, 'btnGestionRoles', 'Permite gestionar roles.', 'Gestión de Roles'),
(8, 'btnGastosPorDia', 'Permite ver gastos diarios.', 'Gastos por Día'),
(9, 'btnGastosPorSemana', 'Permite ver gastos semanales.', 'Gastos por Semana'),
(10, 'btnGastosPorMes', 'Permite ver gastos mensuales.', 'Gastos por Mes'),
(11, 'btnGastosAnual', 'Permite ver gastos anuales.', 'Gastos Anuales'),
(12, 'btnGestionRepartidores', 'Permite gestionar repartidores.', 'Gestión de Repartidores'),
(13, 'permisoVerTodasLasCompras', 'Permite ver las compras de todos los usuarios', 'Ver Todas las Compras');

-- 3️⃣ ROLES_PERMISOS (relación entre roles y permisos)
INSERT INTO `roles_permisos` (`id_rol`, `id_permiso`, `activo`) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 3, 1),
(1, 4, 1),
(1, 5, 1),
(1, 6, 1),
(1, 7, 1),
(1, 8, 1),
(1, 9, 1),
(1, 10, 1),
(1, 11, 1),
(1, 12, 1),
(1, 13, 1),
(2, 1, 1),
(2, 2, 1),
(2, 3, 1),
(2, 4, 1),
(2, 5, 1),
(2, 8, 1),
(2, 9, 1),
(2, 10, 1),
(2, 11, 1),
(2, 12, 1),
(2, 13, 1),
(3, 1, 1),
(3, 2, 1),
(3, 3, 1),
(3, 4, 1),
(3, 5, 1),
(3, 8, 1),
(3, 9, 1),
(3, 10, 1),
(3, 11, 1),
(3, 12, 1),
(3, 13, 1),
(4, 1, 1),
(4, 2, 1),
(4, 3, 1),
(4, 4, 1),
(4, 5, 1),
(4, 8, 1),
(4, 9, 1),
(4, 10, 1),
(4, 11, 1),
(4, 12, 1),
(5, 1, 1),
(5, 2, 1),
(5, 3, 1),
(5, 4, 1),
(5, 5, 1),
(5, 8, 1),
(5, 9, 1),
(5, 10, 1),
(5, 11, 1),
(5, 12, 1);

-- 4️⃣ USUARIOS (ahora que ya existen roles)
INSERT INTO `usuarios` (`id_usuario`, `nombre`, `email`, `password`, `id_rol`) VALUES
(1, 'admin', 'ad@gmail.com', '0cc175b9c0f1b6a831c399e269772661', 1),
(3, 'p', 'p@gmail.com', '83878c91171338902e0fe0fb97a8c47a', 6);

-- 5️⃣ USUARIOS_PERMISOS (por último, porque depende de usuarios y permisos)
INSERT INTO `usuarios_permisos` (`id_usuario`, `id_permiso`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(1, 12),
(1, 13);
