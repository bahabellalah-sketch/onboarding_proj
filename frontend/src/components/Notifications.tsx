import React, { useState, useEffect } from 'react';
import { notificationsApi } from '../services/api';
import { getNotificationIcon, getNotificationLabel, NotificationTypeKey } from '../utils/notificationDisplay';

interface Notification {
  id: number;
  message: string;
  date: string;
  type: NotificationTypeKey | string;
  read: boolean;
  entityType?: string;
  entityId?: number;
}

interface NotificationsProps {
  className?: string;
}

const Notifications: React.FC<NotificationsProps> = (props: NotificationsProps) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);

  const markAsRead = async (id: number) => {
    try {
      await notificationsApi.markNotificationAsRead(id);
      setNotifications(prev => 
        prev.map((notif: Notification) => 
          notif.id === id ? { ...notif, read: true } : notif
        )
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const unreadNotifications = notifications.filter((notif: Notification) => !notif.read);
      const readPromises = unreadNotifications
        .map((notif: Notification) => notificationsApi.markNotificationAsRead(notif.id));

      await Promise.all(readPromises);
      
      setNotifications(prev => 
        prev.map((notif: Notification) => ({ ...notif, read: true }))
      );
      setUnreadCount(0);
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
    }
  };

  const deleteNotification = async (id: number) => {
    try {
      await notificationsApi.deleteNotification(id);
      setNotifications(prev => prev.filter((notif: Notification) => notif.id !== id));
      setUnreadCount(prev => {
        const deletedNotif = notifications.find((notif: Notification) => notif.id === id);
        return deletedNotif && !deletedNotif.read ? prev - 1 : prev;
      });
    } catch (error) {
      console.error('Error deleting notification:', error);
    }
  };

  useEffect(() => {
    // Fetch notifications from backend
    const fetchNotifications = async () => {
      try {
        const response = await notificationsApi.getNotifications();
        if (response && Array.isArray(response)) {
          const notifs = response.map((notif: any) => ({
            id: notif.id || Date.now(),
            message: notif.message || 'Notification',
            date: new Date(notif.dateCreation || '').toLocaleDateString(),
            type: notif.type || 'SYSTEM_ALERT',
            read: notif.read || false,
            entityType: notif.entityType,
            entityId: notif.entityId
          }));
          
          setNotifications(notifs);
          setUnreadCount(notifs.filter((notif: Notification) => !notif.read).length);
        }
      } catch (error) {
        console.error('Error fetching notifications:', error);
      }
    };

    // Fetch notifications every 30 seconds
    const interval = setInterval(fetchNotifications, 30000);
    
    return () => {
      clearInterval(interval);
    };
  }, []);

  return (
    <div className={`notifications-container ${props.className || ''}`}>
      <div className="notifications-header">
        <h3>Notifications</h3>
        {unreadCount > 0 && (
          <span className="unread-badge">{unreadCount}</span>
        )}
        <button onClick={markAllAsRead} className="mark-all-read-btn">
          Mark All as Read
        </button>
      </div>
      
      <div className="notifications-list">
        {notifications.length === 0 ? (
          <div className="no-notifications">
            <p>No notifications</p>
          </div>
        ) : (
          notifications.map(notification => (
            <div 
              key={notification.id} 
              className={`notification-item ${notification.read ? 'read' : 'unread'} ${notification.type}`}
              onClick={() => !notification.read && markAsRead(notification.id)}
            >
              <div className="notification-header">
                <span className="notification-date">{notification.date}</span>
                <span
                  className={`notification-type ${notification.type}`}
                  title={getNotificationLabel(notification.type)}
                >
                  {getNotificationIcon(notification.type)}
                </span>
              </div>
              <span className="notification-type-label">{getNotificationLabel(notification.type)}</span>
              <div className="notification-message">
                {notification.message}
              </div>
            </div>
          ))
        )}
      </div>
    
      <style>{`
        .notifications-container {
          padding: 20px;
          max-width: 800px;
          margin: 0 auto;
        }

        .notifications-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
        }

        .notifications-header h3 {
          margin: 0;
          color: #333;
        }

        .unread-badge {
          background: #e74c3c;
          color: white;
          border-radius: 50%;
          padding: 2px 8px;
          font-size: 12px;
          font-weight: bold;
        }

        .mark-all-read-btn {
          background: #007bff;
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
        }

        .mark-all-read-btn:hover {
          background: #0056b3;
        }

        .notifications-list {
          max-height: 400px;
          overflow-y: auto;
        }

        .notification-item {
          background: white;
          border: 1px solid #e0e0e0;
          border-radius: 8px;
          padding: 12px;
          margin-bottom: 8px;
          cursor: pointer;
          transition: all 0.3s ease;
        }

        .notification-item:hover {
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .notification-item.unread {
          border-left: 4px solid #007bff;
          background: #f8f9fa;
        }

        .notification-item.read {
          border-left: 4px solid #6c757d;
          background: #f8f9fa;
          opacity: 0.7;
        }

        .notification-item.ETAPE_COMPLETE {
          border-left-color: #28a745;
        }

        .notification-item.PARCOURS_COMPLETE {
          border-left-color: #17a2b8;
        }

        .notification-item.ASSIGNMENT_COMPLETE {
          border-left-color: #20c997;
        }

        .notification-item.ASSIGNMENT_OVERDUE {
          border-left-color: #dc3545;
        }

        .notification-item.USER_CREATED {
          border-left-color: #6f42c1;
        }

        .notification-item.USER_UPDATED {
          border-left-color: #664eea3;
        }

        .notification-item.USER_DELETED {
          border-left-color: #e91e63;
        }

        .notification-item.SYSTEM_ALERT {
          border-left-color: #fd7e14;
        }

        .notification-item.DOCUMENT_UPLOADED {
          border-left-color: #198754;
        }

        .notification-item.DOCUMENT_MISSING {
          border-left-color: #ef4444;
        }

        .notification-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
        }

        .notification-date {
          font-size: 12px;
          color: #666;
          font-weight: 500;
        }

        .notification-type {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .notification-message {
          font-size: 14px;
          color: #333;
          line-height: 1.4;
        }

        .no-notifications {
          text-align: center;
          padding: 40px;
          color: #666;
          font-style: italic;
        }
      `}</style>
    </div>
  );
};

export default Notifications;
