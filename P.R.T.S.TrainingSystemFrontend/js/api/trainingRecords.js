(function () {
    'use strict';

    // Training records API
    // GET    /api/v1/user/training-records
    // PUT    /api/v1/user/training-records
    // DELETE /api/v1/user/training-records

    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '');
    const API_PREFIX = BASE + '/api/v1';

    function getToken() {
        return (window.localStorage && (localStorage.getItem('token') || localStorage.getItem('auth_token') || localStorage.getItem('authToken'))) ||
            (window.sessionStorage && (sessionStorage.getItem('token') || sessionStorage.getItem('auth_token') || sessionStorage.getItem('authToken'))) ||
            null;
    }

    window.trainingRecordsApi = {
        get: function () {
            const token = getToken();
            if (!token) return Promise.reject(new Error('请先登录'));
            return fetch(API_PREFIX + '/user/training-records', {
                method: 'GET',
                mode: 'cors',
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return { success: false }; }
            });
        },
        upsert: function (record) {
            const token = getToken();
            if (!token) return Promise.reject(new Error('请先登录'));
            return fetch(API_PREFIX + '/user/training-records', {
                method: 'PUT',
                mode: 'cors',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(record || {})
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return { success: false }; }
            });
        },
        clear: function () {
            const token = getToken();
            if (!token) return Promise.reject(new Error('请先登录'));
            return fetch(API_PREFIX + '/user/training-records', {
                method: 'DELETE',
                mode: 'cors',
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return { success: false }; }
            });
        }
    };
})();
