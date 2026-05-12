import React from 'react';
import type { ApprovalItem } from "../model/ApprovalData";

type Props = {
  order: ApprovalItem | null;
};

function ApprovalDetail({ order }: Props) {
  if (!order) {
    return (
      <div className="card" style={{ height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <p className="text-muted">請從左側選擇要審核的委託單</p>
      </div>
    );
  }

  return (
    <div className="card" style={{ height: '100%', overflowY: 'auto' }}>
      <div className="flex" style={{ justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ margin: 0 }}>委託單詳情 - #{order.id}</h2>
        <span className="tag" style={{ background: '#f3f4f6', color: '#1f2937', fontSize: '12px' }}>
          ID #{order.id}
        </span>
      </div>

      <div className="column">
        {/* 標題欄 */}
        <div className="form-group" style={{ marginBottom: '20px' }}>
          <label className="label" style={{ color: '#6b7280', fontSize: '13px' }}>委託單標題</label>
          <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#111827' }}>{order.title}</div>
        </div>

        {/* 狀態與優先度橫欄 */}
        <div className="flex" style={{ gap: '24px', marginBottom: '24px' }}>
          <div className="form-group" style={{ flex: 1 }}>
            <label className="label" style={{ color: '#6b7280', fontSize: '13px' }}>目前狀態</label>
            <div style={{ marginTop: '4px' }}>
              <span className={`tag ${order.status.toLowerCase()}`}>
                {order.status}
              </span>
            </div>
          </div>
          <div className="form-group" style={{ flex: 1 }}>
            <label className="label" style={{ color: '#6b7280', fontSize: '13px' }}>優先度</label>
            <div style={{ marginTop: '4px' }}>
              <span className={`tag ${order.priority.toLowerCase()}`} 
                    style={{ 
                        background: order.priority === 'URGENT' ? '#fee2e2' : '#f3f4f6',
                        color: order.priority === 'URGENT' ? '#991b1b' : '#374151',
                        border: order.priority === 'URGENT' ? '1px solid #fecaca' : '1px solid #d1d5db'
                    }}>
                {order.priority}
              </span>
            </div>
          </div>
        </div>

        {/* 描述區 */}
        <div className="form-group" style={{ marginBottom: '24px' }}>
          <label className="label" style={{ color: '#6b7280', fontSize: '13px' }}>詳細描述</label>
          <div style={{ 
            marginTop: '8px',
            padding: '16px', 
            background: '#f9fafb', 
            border: '1px solid #f3f4f6', 
            borderRadius: '8px',
            minHeight: '80px',
            lineHeight: '1.6',
            color: '#4b5563'
          }}>
            {order.description || <span className="text-muted">(無詳細描述)</span>}
          </div>
        </div>

        {/* 樣本清單區 */}
        <div className="form-group">
          <label className="label" style={{ color: '#6b7280', fontSize: '13px', marginBottom: '12px', display: 'block' }}>
            樣本與配方清單 ({order.samples?.length || 0})
          </label>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {order.samples && order.samples.length > 0 ? (
              order.samples.map((s, idx) => (
                <div key={idx} style={{ 
                  padding: '12px 16px', 
                  background: '#fff', 
                  border: '1px solid #e5e7eb', 
                  borderRadius: '10px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
                }}>
                  <span style={{ fontWeight: 600, color: '#374151' }}>{s.barcode}</span>
                  <span className="tag" style={{ background: '#e0f2fe', color: '#0369a1', fontSize: '11px', textTransform: 'none' }}>
                    {s.recipeName || '未指定配方'}
                  </span>
                </div>
              ))
            ) : (
              <div className="text-muted" style={{ padding: '20px', textAlign: 'center', border: '1px dashed #d1d5db', borderRadius: '8px' }}>
                此單無樣本資料
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ApprovalDetail;