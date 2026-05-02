package com.eap08.domesticas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageController {

    @GetMapping("/reset-password")
    @ResponseBody
    public String resetPasswordPage(@RequestParam String token) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recuperar contrasena - Domesticas</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                           background: #f5f5f5; display: flex; justify-content: center;
                           align-items: center; min-height: 100vh; }
                    .card { background: white; border-radius: 12px; padding: 32px;
                            max-width: 400px; width: 100%; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                    h2 { color: #333; margin-bottom: 8px; }
                    p { color: #666; margin-bottom: 20px; font-size: 14px; }
                    input { width: 100%; padding: 12px; border: 1px solid #ddd;
                            border-radius: 8px; font-size: 16px; margin-bottom: 12px; }
                    button { width: 100%; padding: 12px; background: #4CAF50; color: white;
                             border: none; border-radius: 8px; font-size: 16px; cursor: pointer; }
                    button:hover { background: #45a049; }
                    button:disabled { background: #ccc; cursor: not-allowed; }
                    .ok { color: #4CAF50; margin-top: 12px; text-align: center; }
                    .error { color: #f44336; margin-top: 12px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="card">
                    <h2>Recuperar contrasena</h2>
                    <p>Ingresa tu nueva contrasena (minimo 8 caracteres).</p>
                    <input type="password" id="password" placeholder="Nueva contrasena" minlength="8">
                    <button id="btn" onclick="cambiar()">Cambiar contrasena</button>
                    <p id="msg"></p>
                </div>
                <script>
                async function cambiar() {
                    var pass = document.getElementById('password').value;
                    if (pass.length < 6) {
                        document.getElementById('msg').className = 'error';
                        document.getElementById('msg').textContent = 'La contrasena debe tener al menos 6 caracteres';
                        return;
                    }
                    var btn = document.getElementById('btn');
                    btn.disabled = true;
                    btn.textContent = 'Procesando...';
                    try {
                        var resp = await fetch('/api/auth/reset-password', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ token: 'TOKEN_PLACEHOLDER', nuevaPassword: pass })
                        });
                        var data = await resp.json();
                        var msg = document.getElementById('msg');
                        msg.className = resp.ok ? 'ok' : 'error';
                        msg.textContent = resp.ok ? 'Contrasena cambiada correctamente' : (data.message || 'Error');
                    } catch(e) {
                        var msg = document.getElementById('msg');
                        msg.className = 'error';
                        msg.textContent = 'Error de conexion. Intenta de nuevo.';
                    }
                    btn.disabled = false;
                    btn.textContent = 'Cambiar contrasena';
                }
                </script>
            </body>
            </html>
            """.replace("TOKEN_PLACEHOLDER", token);
    }

    @GetMapping("/join")
    @ResponseBody
    public String joinPage(@RequestParam String token) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Invitacion a hogar - Domesticas</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                           background: #f5f5f5; display: flex; justify-content: center;
                           align-items: center; min-height: 100vh; }
                    .card { background: white; border-radius: 12px; padding: 32px;
                            max-width: 400px; width: 100%; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                            text-align: center; }
                    h2 { color: #333; margin-bottom: 12px; }
                    p { color: #666; margin-bottom: 24px; font-size: 15px; }
                    .buttons { display: flex; gap: 12px; justify-content: center; }
                    button { padding: 12px 28px; border: none; border-radius: 8px;
                             font-size: 16px; cursor: pointer; color: white; }
                    .aceptar { background: #4CAF50; }
                    .aceptar:hover { background: #45a049; }
                    .rechazar { background: #f44336; }
                    .rechazar:hover { background: #d32f2f; }
                    button:disabled { background: #ccc; cursor: not-allowed; color: #888; }
                    .ok { color: #4CAF50; margin-top: 16px; }
                    .error { color: #f44336; margin-top: 16px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <h2>Invitacion a un hogar</h2>
                    <p>Has sido invitado a unirte a un hogar en Domesticas.<br>Que deseas hacer?</p>
                    <div class="buttons">
                        <button class="aceptar" id="btnAceptar" onclick="responder('ACEPTAR')">Aceptar</button>
                        <button class="rechazar" id="btnRechazar" onclick="responder('RECHAZAR')">Rechazar</button>
                    </div>
                    <p id="msg"></p>
                </div>
                <script>
                async function responder(accion) {
                    document.getElementById('btnAceptar').disabled = true;
                    document.getElementById('btnRechazar').disabled = true;
                    try {
                        var resp = await fetch('/api/households/invitations/TOKEN_PLACEHOLDER/respond', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ accion: accion })
                        });
                        var data = await resp.json();
                        var msg = document.getElementById('msg');
                        msg.className = resp.ok ? 'ok' : 'error';
                        var texto = accion === 'ACEPTAR' ? 'aceptada' : 'rechazada';
                        msg.textContent = resp.ok ? 'Invitacion ' + texto + '. Bienvenido!' : (data.message || 'Error');
                    } catch(e) {
                        var msg = document.getElementById('msg');
                        msg.className = 'error';
                        msg.textContent = 'Error de conexion. Intenta de nuevo.';
                    }
                }
                </script>
            </body>
            </html>
            """.replace("TOKEN_PLACEHOLDER", token);
    }
}
