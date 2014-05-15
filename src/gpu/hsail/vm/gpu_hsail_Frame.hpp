#ifndef GPU_HSAIL_FRAME_HPP
#define GPU_HSAIL_FRAME_HPP

#include "graal/graalEnv.hpp"
#include "code/debugInfo.hpp"
#include "code/location.hpp"

// maximum registers that could be saved for now
#define MAX_SREGS 32
#define MAX_DREGS 16

class HSAILFrame {
  friend class VMStructs;
private:
  jint  _pc_offset;  // The HSAIL "pc_offset" where the exception happens
  jbyte _num_s_regs;
  jbyte _num_d_regs;
  jshort _dreg_oops_map;  // bits = 1 if that dreg is an oop
  jlong  _save_area[MAX_SREGS/2 + MAX_DREGS];  

public:
  // Accessors
  jint pc_offset() { return _pc_offset; }
  jint num_s_regs() {return _num_s_regs; }
  jint num_d_regs() {return _num_d_regs; }
  jint dreg_oops_map() {return _dreg_oops_map; }
  jlong get_d_reg(int idx) {
    char *p = (char *) _save_area;
    int ofst = num_s_regs() * 4 + idx * 8;
    return(*(jlong *) (p + ofst));
  }
  jint get_s_reg(int idx) {
    char *p = (char *) _save_area;
    int ofst = idx * 4;
    return(*(jint *) (p + ofst));
  }
  void put_d_reg(int idx, jlong val) {
    char *p = (char *) _save_area;
    int ofst = num_s_regs() * 4 + idx * 8;
    (*(jlong *) (p + ofst)) = val;
  }
};
  
#endif // GPU_HSAIL_FRAME_HPP
