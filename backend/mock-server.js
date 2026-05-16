const http = require('http');
const url = require('url');

const port = 8080;

const mockData = {
  token: 'mock-jwt-token-for-demo',
  user: {
    id: 1,
    username: 'demo',
    email: 'demo@example.com',
    avatar: null
  }
};

const handleRequest = (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;
  const method = req.method;

  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }

  console.log(`${method} ${pathname}`);

  if (pathname === '/api/auth/login' && method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const data = JSON.parse(body);
        if (data.username && data.password) {
          res.writeHead(200);
          res.end(JSON.stringify({
            code: 200,
            message: '登录成功',
            data: { token: mockData.token, ...mockData.user }
          }));
        } else {
          res.writeHead(401);
          res.end(JSON.stringify({ code: 401, message: '用户名或密码错误' }));
        }
      } catch (e) {
        res.writeHead(400);
        res.end(JSON.stringify({ code: 400, message: '请求格式错误' }));
      }
    });
  } else if (pathname === '/api/auth/register' && method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const data = JSON.parse(body);
        res.writeHead(200);
        res.end(JSON.stringify({
          code: 200,
          message: '注册成功',
          data: { id: 1, username: data.username, email: data.email }
        }));
      } catch (e) {
        res.writeHead(400);
        res.end(JSON.stringify({ code: 400, message: '请求格式错误' }));
      }
    });
  } else if (pathname === '/api/auth/userinfo' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: mockData.user
    }));
  } else if (pathname === '/api/note/list' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: {
        list: [
          { id: 1, title: '欢迎使用记事本', content: '# 欢迎\n\n这是一个演示笔记', summary: '欢迎使用记事本', created_at: '2026-05-16 10:00:00', updated_at: '2026-05-16 10:00:00' }
        ],
        total: 1,
        page: 1,
        pageSize: 10
      }
    }));
  } else if (pathname.startsWith('/api/note') && method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const data = JSON.parse(body);
        res.writeHead(200);
        res.end(JSON.stringify({
          code: 200,
          message: '操作成功',
          data: { id: Date.now(), ...data }
        }));
      } catch (e) {
        res.writeHead(400);
        res.end(JSON.stringify({ code: 400, message: '请求格式错误' }));
      }
    });
  } else if (pathname === '/api/category/list' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: [
        { id: 1, name: '默认分类', sort_order: 0 }
      ]
    }));
  } else if (pathname === '/api/tag/list' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: [
        { id: 1, name: '重要', color: '#ff4d4f' },
        { id: 2, name: '工作', color: '#1890ff' }
      ]
    }));
  } else {
    res.writeHead(404);
    res.end(JSON.stringify({ code: 404, message: '接口不存在' }));
  }
};

const server = http.createServer(handleRequest);

server.listen(port, '0.0.0.0', () => {
  console.log(`Mock API Server running at http://0.0.0.0:${port}/`);
  console.log('Available endpoints:');
  console.log('  POST /api/auth/login');
  console.log('  POST /api/auth/register');
  console.log('  GET  /api/auth/userinfo');
  console.log('  GET  /api/note/list');
  console.log('  POST /api/note/create');
  console.log('  GET  /api/category/list');
  console.log('  GET  /api/tag/list');
});
