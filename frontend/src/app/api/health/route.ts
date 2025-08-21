// Health check endpoint for Next.js
export async function GET() {
  return Response.json({ 
    status: 'ok', 
    timestamp: new Date().toISOString(),
    service: 'Story Publisher Frontend' 
  })
}
