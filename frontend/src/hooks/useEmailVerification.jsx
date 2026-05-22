import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { API_BASE_URL } from '../config/apiConfig';

const useEmailVerification = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isVerified, setIsVerified] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkVerification = async () => {
      console.log('useEmailVerification: Checking verification status');
      console.log('useEmailVerification: Current path:', location.pathname);
      console.log('useEmailVerification: Is authenticated:', isAuthenticated);
      
      setIsLoading(true);
      
      // Allow access to profile pages regardless of verification status
      if (location.pathname === '/profile' || location.pathname.startsWith('/profile/')) {
        console.log('useEmailVerification: On profile page - allowing access');
        setIsVerified(true);
        setIsLoading(false);
        return;
      }

      // Allow access to login page
      if (location.pathname === '/login') {
        console.log('useEmailVerification: On login page - allowing access');
        setIsVerified(true);
        setIsLoading(false);
        return;
      }

      // Allow access to verify-email page for email verification
      if (location.pathname === '/verify-email') {
        console.log('useEmailVerification: On verify-email page - allowing access');
        setIsVerified(true);
        setIsLoading(false);
        return;
      }

      // Allow access to team page
      if (location.pathname === '/team') {
        console.log('useEmailVerification: On team page - allowing access');
        setIsVerified(true);
        setIsLoading(false);
        return;
      }

      // If not authenticated, allow access (for login flow)
      if (!isAuthenticated) {
        console.log('useEmailVerification: Not authenticated - allowing access');
        setIsVerified(true);
        setIsLoading(false);
        return;
      }

      // Check verification status from database via API
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/email-verification/current-user-status`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        const verified = response.data.verified;
        console.log('useEmailVerification: Verified status from database:', verified);
        
        setIsVerified(verified);
        setIsLoading(false);

        // If not verified, redirect to profile
        if (!verified) {
          console.log('useEmailVerification: User not verified - REDIRECTING TO PROFILE');
          navigate('/profile', { replace: true });
        } else {
          console.log('useEmailVerification: User verified - allowing access');
        }
      } catch (error) {
        console.error('useEmailVerification: Error checking verification status:', error);
        // If API fails, assume user is not verified for safety
        setIsVerified(false);
        setIsLoading(false);
        console.log('useEmailVerification: API error - assuming unverified, redirecting to profile');
        navigate('/profile', { replace: true });
      }
    };

    checkVerification();
  }, [isAuthenticated, location.pathname, navigate]);

  return { isVerified, isLoading, isAuthenticated };
};

export default useEmailVerification;
