import jwt from 'jsonwebtoken'
import * as dotenv from 'dotenv'
dotenv.config();

 const auth = async (req,res,next) => {

    try {
        const token = req.headers.authorization?.split(' ')[1];
        let decodeData ;
         if(token) {  
            decodeData = jwt.verify(token,process.env.SECRET) ;
            req.userId = decodeData?.id
         }
         next();
    } catch (error) {
        console.log(error)
    }
}
export default  auth