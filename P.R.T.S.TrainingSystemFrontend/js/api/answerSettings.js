(function () {
    'use strict';

    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '');
    const API_PREFIX = BASE + '/api/v1';

    function getToken() {
        return (window.localStorage && (localStorage.getItem('token') || localStorage.getItem('auth_token'))) ||
            (window.sessionStorage && (sessionStorage.getItem('token') || sessionStorage.getItem('auth_token'))) ||
            null;
    }

    window.answerSettingsApi = {
        get: function () {
            const token = getToken();
            if (!token) return Promise.reject(new Error('请先登录'));
            return fetch(API_PREFIX + '/user/answer-settings', {
                method: 'GET',
                mode: 'cors',
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return { success: false }; }
            });
        },
        update: function (payload) {
            const token = getToken();
            if (!token) return Promise.reject(new Error('请先登录'));
            return fetch(API_PREFIX + '/user/answer-settings', {
                method: 'PUT',
                mode: 'cors',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(payload || {})
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return { success: false }; }
            });
        }
    };
})();

